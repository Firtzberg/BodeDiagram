package com.hrca.bode.bodediagram;

import org.ejml.data.Complex64F;

/**
 * Created by hrvoje on 31.07.15..
 */
public class TFCalculator implements TFCalculatorInterface{

    protected final int astatism;
    protected final double[] numeratorVector;
    protected final double[] denominatorVector;
    private final Complex64F reusableComplex = new Complex64F();

    public TFCalculator(int astatism, double[] numeratorVector, double[] denominatorVector){
        this.astatism = astatism;
        this.numeratorVector = numeratorVector;
        this.denominatorVector = denominatorVector;
    }

    private Complex64F calculatePolynomialValue(double frequency, double[] coefficients){
        int index = coefficients.length - 1;
        double resultReal = coefficients[index];
        double resultImaginary = 0;
        double tmp;
        for (index --; index >= 0; index --) {
            tmp = resultReal;
            resultReal = -resultImaginary*frequency + coefficients[index];
            resultImaginary = tmp * frequency;
        }
        this.reusableComplex.real = resultReal;
        this.reusableComplex.imaginary = resultImaginary;
        return this.reusableComplex;
    }

    public Point calculatePoint(double frequency){
        Complex64F value = calculatePolynomialValue(frequency, numeratorVector);
        double amplitude = value.getMagnitude();
        double phase = Math.atan2(value.imaginary, value.real);
        value = calculatePolynomialValue(frequency, denominatorVector);
        double m = value.getMagnitude();
        if(m == 0) {
            amplitude = Double.POSITIVE_INFINITY;
        }
        else{
            amplitude /= value.getMagnitude();
        }
        phase -= Math.atan2(value.imaginary, value.real);
        phase *= 180/Math.PI;
        amplitude *= Math.pow(frequency, astatism);
        phase += astatism * 90;

        return new Point(amplitude, phase, frequency);
    }

    public Point[] calculatePoints(double[] frequencies){
        if(frequencies == null)
            return null;
        Point[] points = new Point[frequencies.length];
        for(int i = 0; i < points.length; i ++)
            points[i] = this.calculatePoint(frequencies[i]);
        return points;
    }
}
