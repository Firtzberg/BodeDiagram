package com.hrca.bode.bodediagram;

/**
 * Created by hrvoje on 27.07.15..
 */

public class Point{
    public final float amplitudeDB;
    public final float phaseDegree;
    public final float frequencyLog10;

    public Point(double amplitude, double phaseDegree, double frequency){
        if(amplitude < 0) {
            phaseDegree -= 180;
            amplitude = -amplitude;
        }
        phaseDegree -= Math.floor((phaseDegree + 270) / 360)*360;
        this.amplitudeDB = 20*(float)Math.log10(amplitude);
        this.phaseDegree = (float)phaseDegree;
        this.frequencyLog10 = (float)Math.log10(frequency);
    }
}
