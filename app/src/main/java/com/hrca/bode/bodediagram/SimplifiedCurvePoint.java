package com.hrca.bode.bodediagram;

/**
 * Created by hrvoje on 17.08.15..
 */
public class SimplifiedCurvePoint{
    public final float frequencyLog10;
    public final float amplitudeDB;

    public SimplifiedCurvePoint(double frequencyLog10, double amplitudeDB){
        float tmp = (float)frequencyLog10;
        if(Math.abs(Math.round(tmp) - tmp) < 0.02){
            tmp = Math.round(tmp);
        }
        this.frequencyLog10 =tmp;
        tmp = (float)amplitudeDB;
        if(Math.abs(Math.round(tmp) - tmp) < 0.02){
            tmp = Math.round(tmp);
        }
        this.amplitudeDB = tmp;
    }
}
