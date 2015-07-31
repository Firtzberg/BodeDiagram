package com.hrca.bode.bodediagram;

import java.util.ArrayList;

/**
 * Created by hrvoje on 31.07.15..
 */
public class SimplifiedCurve {

    public class SimplifiedCurvePoint{
        public final double frequency;
        public final float amplitudeDB;

        public SimplifiedCurvePoint(double frequency, double amplitudeDB){
            this.frequency = frequency;
            this.amplitudeDB = (float)amplitudeDB;
        }
    }

    public class SimplifiedCurvePart{
        int gradientCoefficient;
        public final double start;

        public SimplifiedCurvePart(double start, int astatism){
            this.start = start;
            this.gradientCoefficient = astatism;
        }

        public SimplifiedCurvePoint calculatePoint(SimplifiedCurvePart previous, double valueAt1){
            return new SimplifiedCurvePoint(this.start, valueAt1 + 20*previous.gradientCoefficient*Math.log10(this.start));
        }

        public double nextValueAt1(SimplifiedCurvePart previous, double valueAt1){
            return valueAt1 + 20*Math.log10(this.start)*(previous.gradientCoefficient - this.gradientCoefficient);
        }
    }

    double K;
    private final ArrayList<SimplifiedCurvePart> parts;

    public SimplifiedCurve(int astatism, double gain){
        K = 20*Math.log10(gain);
        parts = new ArrayList<>();
        SimplifiedCurvePart specter = new SimplifiedCurvePart(Double.NEGATIVE_INFINITY, astatism);
        parts.add(specter);
    }

    public void split(double frequency, boolean isZero){
        int i;
        for(i = 1; i < parts.size(); i++){
            if(frequency <= parts.get(i).start)
                break;
        }
        if(i == parts.size() || parts.get(i).start != frequency){
            parts.add(i,new SimplifiedCurvePart(frequency, parts.get(i - 1).gradientCoefficient));
        }
        if(isZero)
            for(;i < parts.size(); i ++){
                parts.get(i).gradientCoefficient ++;
            }
        else
            for(;i < parts.size(); i ++){
                parts.get(i).gradientCoefficient --;
            }
    }

    public SimplifiedCurvePoint[] getPoints(){
        SimplifiedCurvePoint[] points = new SimplifiedCurvePoint[parts.size() + 1];
        double k = K;
        int i;
        for(i = 1; i < parts.size(); i ++){
            points[i] = parts.get(i).calculatePoint(parts.get(i - 1), k);
            k = parts.get(i).nextValueAt1(parts.get(i - 1), k);
        }
        double f = parts.get(i - 1).start;
        if(f == Double.NEGATIVE_INFINITY)
            f = 1;
        f *= 10;
        SimplifiedCurvePart last = new SimplifiedCurvePart(f, parts.get(i - 1).gradientCoefficient);
        points[i] = last.calculatePoint(parts.get(i - 1), k);
        k = K;
        if(parts.size() > 1)
            f = parts.get(1).start;
        else f = 1;
        f /= 10;
        SimplifiedCurvePart first = new SimplifiedCurvePart(f, parts.get(0).gradientCoefficient);
        points[0] = first.calculatePoint(parts.get(0), k);
        return points;
    }
}
