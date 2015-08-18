package com.hrca.bode.bodediagram;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import com.hrca.bode.customs.HistoryHelper;
import com.hrca.bode.customs.displaycustoms.TransferFunctionView;

import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.EigenDecomposition;

import java.util.ArrayList;

public class ResultActivity extends Activity {
    public static final String EXTRA_COORDINATES = "coordinates";
    public static final int REQUEST_CODE_COORDINATES = 672;
    public static final String PARCELABLE_FORMATTED_TF = "formattedTF";
    public static final String PARCELABLE_ORIGINAL_TF = "originalTF";
    private static final double FREQUENCY_DENSITY = 20;
    protected TransferFunctionView originalTransferFunction;
    protected TransferFunctionView formattedTransferFunction;
    protected SimplifiedCurvePoint[] simplifiedCurvePoints;
    protected DiagramView diagram;

    private class PolynomialChainParameters{
        public final ArrayList<Complex64F> roots;
        public final double[] vector;
        public final double gain;
        public final int astatism;

        public PolynomialChainParameters(ArrayList<Complex64F> roots, double[] vector, double gain, int astatism){
            this.roots = roots;
            this.vector = vector;
            this.gain = gain;
            this.astatism = astatism;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        this.originalTransferFunction = (TransferFunctionView) findViewById(R.id.original);
        this.formattedTransferFunction = (TransferFunctionView) findViewById(R.id.formatted);
        this.diagram = (DiagramView)findViewById(R.id.diagram);

        Intent request = this.getIntent();
        Parcelable tf = request.getParcelableExtra(InputActivity.EXTRA_TRANSFER_FUNCTION);
        if(tf != null) {
            this.originalTransferFunction.onRestoreInstanceState(tf);

            double gain = this.originalTransferFunction.getGain();
            if(gain == 0) {
                finishWithError(R.string.gain_zero);
                return;
            }

            int astatism = this.originalTransferFunction.getAstatism();

            PolynomialChainParameters numeratorParameters =
                    AnalysePolynomialChain(this.originalTransferFunction.getNumeratorCoefficientArrays());
            if(numeratorParameters.gain == 0) {
                finishWithError(R.string.numerator_zero);
                return;
            }
            gain *= numeratorParameters.gain;
            astatism += numeratorParameters.astatism;

            PolynomialChainParameters denominatorParameters =
                    AnalysePolynomialChain(this.originalTransferFunction.getDenominatorCoefficientArrays());
            if(denominatorParameters.gain == 0) {
                finishWithError(R.string.denominator_zero);
                return;
            }
            astatism -= denominatorParameters.astatism;
            gain /= denominatorParameters.gain;
            for(int i = 0; i < numeratorParameters.vector.length; i ++){
                numeratorParameters.vector[i] *= gain;
            }

            SimplifiedCurve curve = new SimplifiedCurve(astatism, gain);
            for(Complex64F zero : numeratorParameters.roots)
                curve.split(zero.getMagnitude(), true);
            for(Complex64F pole : denominatorParameters.roots)
                curve.split(pole.getMagnitude(), false);
            simplifiedCurvePoints = curve.getPoints();

            this.formattedTransferFunction.addNumeratorRoots(numeratorParameters.roots);
            this.formattedTransferFunction.addDenominatorRoots(denominatorParameters.roots);
            this.formattedTransferFunction.setAstatism(astatism);
            this.formattedTransferFunction.adjustMainFractalVisibility();
            this.formattedTransferFunction.setGain(gain);

            HistoryHelper.add(this.originalTransferFunction);

            TFCalculatorInterface calculator = new TFCalculator(astatism, numeratorParameters.vector, denominatorParameters.vector);
            Point[] exactPoints = calculator.calculatePoints(getFrequencies(simplifiedCurvePoints[0].frequencyLog10,
                    simplifiedCurvePoints[simplifiedCurvePoints.length - 1].frequencyLog10));
            this.diagram.setPoints(exactPoints, simplifiedCurvePoints);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ResultActivity.this.diagram.redraw();
            }
        }).start();
    }

    public void showCoordinates(View view) {
        Intent intent = new Intent(this, CoordinatesActivity.class);
        float[] coordinates = new float[2 * simplifiedCurvePoints.length];
        for(int i = 0; i < simplifiedCurvePoints.length; i ++){
            coordinates[2*i] = simplifiedCurvePoints[i].frequencyLog10;
            coordinates[2*i + 1] = simplifiedCurvePoints[i].amplitudeDB;
        }
        intent.putExtra(EXTRA_COORDINATES, coordinates);
        startActivityForResult(intent, REQUEST_CODE_COORDINATES);
    }

    private void finishWithError(int messageIdentifier){
        Intent myIntent = new Intent();
        myIntent.putExtra(InputActivity.EXTRA_DISPLAY_ERROR_MESSAGE_R_ID, messageIdentifier);
        this.setResult(RESULT_OK, myIntent);
        this.finish();
    }

    private PolynomialChainParameters AnalysePolynomialChain(double[][] coefficientArrays) {
        ArrayList<Complex64F> roots = new ArrayList<>();
        double[] Tmp;
        double[] coefficients;
        int totalCoefficients = 1;
        int astatismChange = 0;
        double gainChange = 1;
        int firstNonZero;
        int realLength;

        for (int i = 0; i < coefficientArrays.length; i++) {
            double[] coefficientArray = coefficientArrays[i];
            if (coefficientArray.length < 1)
                continue;
            for (firstNonZero = 0; firstNonZero < coefficientArray.length; firstNonZero++) {
                if (coefficientArray[firstNonZero] != 0) {
                    gainChange *= coefficientArray[firstNonZero];
                    break;
                }
            }
            if (firstNonZero == coefficientArray.length) {
                return new PolynomialChainParameters(roots, new double[]{1.0}, 0, 0);
            }
            for (realLength = coefficientArray.length; realLength > 0; realLength--) {
                if (coefficientArray[realLength - 1] != 0) {
                    realLength -= firstNonZero;
                    break;
                }
            }
            if (realLength != coefficientArray.length) {
                Tmp = new double[realLength];
                System.arraycopy(coefficientArray, firstNonZero, Tmp, 0, realLength);
                coefficientArrays[i] = coefficientArray = Tmp;
                astatismChange += firstNonZero;
            }
            totalCoefficients += realLength - 1;

            for (Complex64F root : findRoots(coefficientArray)) {
                // All roots except 0.
                // Zero can't occur.
                roots.add(root);
            }
        }

        double[] formerCoefficients = new double[totalCoefficients];
        coefficients = new double[totalCoefficients];
        coefficients[0] = 1;
        int filled = 1;
        int i, j = 0;
        for (double[] coefficientArray : coefficientArrays) {
            if (coefficientArray.length == 0)
                continue;
            for(i = 0; i < filled; i ++){
                formerCoefficients[i] = 0;
            }
            for (i = 0; i < filled; i++) {
                for (j = 0; j < coefficientArray.length; j++) {
                    formerCoefficients[i + j] += coefficients[i] * coefficientArray[j];
                }
            }
            filled += j - 1;
            Tmp = coefficients;
            coefficients = formerCoefficients;
            formerCoefficients = Tmp;
        }

        for(i = 0; i < coefficients.length; i ++)
            coefficients[i] /= gainChange;

        return new PolynomialChainParameters(roots, coefficients, gainChange, astatismChange);
    }

    private double[] getFrequencies(double minFrequencyLog10, double maxFrequencyLog10){
        double step = 1/FREQUENCY_DENSITY;
        int total = (int)((maxFrequencyLog10 - minFrequencyLog10)*FREQUENCY_DENSITY) + 1;
        double[] result = new double[total];
        double current = minFrequencyLog10;
        for(int i = 0 ; i < total; i++, current += step){
            result[i] = Math.pow(10, current);
        }
        return result;
    }

    public static Complex64F[] findRoots(double... coefficients) {
        int N = coefficients.length;
        double a = 0;
        while(a == 0 && N > 0){
            N --;
            a = coefficients[N];
        }

        if(N == 0)
            return new Complex64F[0];

        // Construct the companion matrix
        DenseMatrix64F c = new DenseMatrix64F(N,N);

        for( int i = 0; i < N; i++ ) {
            c.set(i,N-1,-coefficients[i]/a);
        }
        for( int i = 1; i < N; i++ ) {
            c.set(i,i-1,1);
        }

        // use generalized eigenvalue decomposition to find the roots
        EigenDecomposition<DenseMatrix64F> evd =  DecompositionFactory.eig(N,false);

        evd.decompose(c);

        Complex64F[] roots = new Complex64F[N];

        for( int i = 0; i < N; i++ ) {
            roots[i] = evd.getEigenvalue(i);
        }

        return roots;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(PARCELABLE_ORIGINAL_TF, this.originalTransferFunction.onSaveInstanceState());
        savedInstanceState.putParcelable(PARCELABLE_FORMATTED_TF, this.formattedTransferFunction.onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.originalTransferFunction.onRestoreInstanceState(savedInstanceState.getParcelable(PARCELABLE_ORIGINAL_TF));
        this.formattedTransferFunction.onRestoreInstanceState(savedInstanceState.getParcelable(PARCELABLE_FORMATTED_TF));
    }
}
