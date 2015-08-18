package com.hrca.bode.bodediagram;

/**
 * Created by hrvoje on 17.08.15..
 */
public class SimplifiedCurvePoint{
    public final float frequencyLog10;
    public final float amplitudeDB;

    public SimplifiedCurvePoint(double frequencyLog10, double amplitudeDB){
        this.frequencyLog10 = (float)frequencyLog10;
        this.amplitudeDB = (float)amplitudeDB;
    }
}
