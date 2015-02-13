package com.hrca.bode.bodediagram;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import com.hrca.bode.customs.TransferFunctionView;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.EigenDecomposition;

public class ResultActivity extends Activity {
    protected TransferFunctionView originalTransferFunction;
    protected TransferFunctionView formattedTransferFunction;
    protected DiagramView diagram;
    double[] numeratorVector;
    double[] denominatorVector;
    double minFrequency;
    double maxFrequency;

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
            int astatism = this.originalTransferFunction.getAstatism();
            boolean frequencyFound = false;
            if(astatism != 0){
                minFrequency = maxFrequency = 1;
                frequencyFound = true;
            }
            double[][] numeratorCoefficientArrays = this.originalTransferFunction.getNumeratorCoefficientArrays();
            double[][] denominatorCoefficientArrays = this.originalTransferFunction.getDenominatorCoefficientArrays();
            Complex64F[] roots;
            double[] Tmp;
            double[] coefficients;
            int totalCoefficients = 1;
            double frequency;
            for (double[] numeratorCoefficientArray : numeratorCoefficientArrays) {
                if (numeratorCoefficientArray.length > 1)
                    totalCoefficients += numeratorCoefficientArray.length - 1;
                for (int j = 0; j < numeratorCoefficientArray.length; j++) {
                    if (numeratorCoefficientArray[j] != 0) {
                        gain *= numeratorCoefficientArray[j];
                        astatism += j;
                        break;
                    }
                }

                roots = findRoots(numeratorCoefficientArray);
                for(Complex64F root : roots){
                    frequency = (float) root.getReal();
                    if(frequency == 0)
                        continue;
                    if(frequency < 0)
                        frequency = -frequency;
                    if(frequencyFound) {
                        if (frequency < minFrequency)
                            minFrequency = frequency;
                        if (frequency > maxFrequency)
                            maxFrequency = frequency;
                    }
                    else{
                        maxFrequency = minFrequency = frequency;
                        frequencyFound = true;
                    }
                }
                this.formattedTransferFunction.addNumeratorRoots(roots);
            }
            coefficients = new double[totalCoefficients];
            coefficients[0] = this.originalTransferFunction.getGain();
            int filled = 1;
            for (double[] numeratorCoefficientArray : numeratorCoefficientArrays) {
                int j, k = 0;
                if (numeratorCoefficientArray.length == 0)
                    continue;
                Tmp = new double[totalCoefficients];
                for (j = 0; j < filled; j++) {
                    for (k = 0; k < numeratorCoefficientArray.length; k++) {
                        Tmp[j + k] += coefficients[j] * numeratorCoefficientArray[k];
                    }
                }
                filled += k - 1;
                coefficients = Tmp;
            }
            
            numeratorVector = coefficients;
            
            totalCoefficients = 1;
            for (double[] denominatorCoefficientArray : denominatorCoefficientArrays) {
                if (denominatorCoefficientArray.length > 1)
                    totalCoefficients += denominatorCoefficientArray.length - 1;
                for (int j = 0; j < denominatorCoefficientArray.length; j++) {
                    if (denominatorCoefficientArray[j] != 0) {
                        gain /= denominatorCoefficientArray[j];
                        astatism -= j;
                        break;
                    }
                }
                roots = findRoots(denominatorCoefficientArray);
                for(Complex64F root : roots){
                    frequency = (float) root.getReal();
                    if(frequency == 0)
                        continue;
                    if(frequency < 0)
                        frequency = -frequency;
                    if(frequencyFound) {
                        if (frequency < minFrequency)
                            minFrequency = frequency;
                        if (frequency > maxFrequency)
                            maxFrequency = frequency;
                    }
                    else{
                        maxFrequency = minFrequency = frequency;
                        frequencyFound = true;
                    }
                }
                this.formattedTransferFunction.addDenominatorRoots(roots);
            }

            coefficients = new double[totalCoefficients];
            coefficients[0] = 1;
            filled = 1;
            for (double[] denominatorCoefficientArray : denominatorCoefficientArrays) {
                int j, k = 0;
                if (denominatorCoefficientArray.length == 0)
                    continue;
                Tmp = new double[totalCoefficients];
                for (j = 0; j < filled; j++) {
                    for (k = 0; k < denominatorCoefficientArray.length; k++) {
                        Tmp[j + k] += coefficients[j] * denominatorCoefficientArray[k];
                    }
                }
                filled += k - 1;
                coefficients = Tmp;
            }
            if(!frequencyFound){
                minFrequency = maxFrequency = 1;
            }
            denominatorVector = coefficients;
            minFrequency = (float)Math.log10(minFrequency);
            maxFrequency = (float)Math.log10(maxFrequency);

            this.formattedTransferFunction.setGain(gain);
            this.formattedTransferFunction.setAstatism(astatism);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        this.diagram.draw(this.formattedTransferFunction.getAstatism(),
                        numeratorVector, denominatorVector, minFrequency, maxFrequency);
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
}
