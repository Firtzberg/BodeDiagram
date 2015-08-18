package com.hrca.bode.bodediagram;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class DiagramView extends SurfaceView {

    private static final float DENSITY_COEFFICIENT = Resources.getSystem().getDisplayMetrics().density;
    private static final float PIXELS_PER_DECADE = 60 * DENSITY_COEFFICIENT;
    private static final float PIXELS_PER_DB = 2.5F * DENSITY_COEFFICIENT;
    private static final float PIXELS_PER_DEGREE = 50/90F * DENSITY_COEFFICIENT;
    private static final float PIXELS_LEFT_PADDING = 50 * DENSITY_COEFFICIENT;
    private static final float PIXELS_TOP_PADDING = 20 * DENSITY_COEFFICIENT;
    private static final float PIXELS_RIGHT_PADDING = 20 * DENSITY_COEFFICIENT;
    private static final float PIXELS_BOTTOM_PADDING = 20 * DENSITY_COEFFICIENT;
    private static final float PIXELS_BETWEEN_DIAGRAMS = 15 * DENSITY_COEFFICIENT;
    private static final float linesLength = 5 * DENSITY_COEFFICIENT;
    private static final float AMPLITUDE_STEP_DB = 20;
    private static final float PHASE_STEP_DEGREES = 45;
    private static final int COLOR_DEFAULT_BACKGROUND = Color.WHITE;
    private static final int COLOR_DEFAULT_LINES = Color.LTGRAY;
    private static final int COLOR_DEFAULT_AXIS = Color.BLACK;
    private static final int COLOR_DEFAULT_CURVE = Color.BLUE;
    private static final int COLOR_DEFAULT_SIMPLIFIED_CURVE = Color.RED;
    private static final int COLOR_DEFAULT_TEXT = Color.BLACK;
    private static final float SIZE_DEFAULT_TEXT = 12 * Resources.getSystem().getDisplayMetrics().scaledDensity;
    private static final float RELATIVE_CURVE_THICKNESS = 1.5F;
    private Point[] points;
    private SimplifiedCurvePoint[] simplifiedCurvePoints;
    private float minFrequency;
    private float maxFrequency;
    private float minAmplitude;
    private float maxAmplitude;
    private float minPhase = -270;
    private float maxPhase = 90;
    private int backgroundColor;
    private final Paint linesPaint;
    private final Paint axisPaint;
    private final Paint curvePaint;
    private final Paint simplifiedCurvePaint;
    private final Paint textPaint;

    public DiagramView(Context context) {
        this(context, null);
    }

    public DiagramView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.linesPaint = new Paint();
        this.linesPaint.setStrokeWidth(DENSITY_COEFFICIENT);
        this.axisPaint = new Paint();
        this.axisPaint.setStrokeWidth(DENSITY_COEFFICIENT);
        this.curvePaint = new Paint();
        this.curvePaint.setStrokeWidth(RELATIVE_CURVE_THICKNESS * DENSITY_COEFFICIENT);
        this.simplifiedCurvePaint = new Paint();
        this.simplifiedCurvePaint.setStrokeWidth(RELATIVE_CURVE_THICKNESS * DENSITY_COEFFICIENT);
        this.textPaint = new Paint();
        this.textPaint.setStrokeWidth(DENSITY_COEFFICIENT);

        init(attrs);
    }

    private void init(AttributeSet attrs){
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DiagramView,
                0, 0);
        int axisColor;
        int curveColor;
        int simplifiedCurveColor;
        int linesColor;
        int textColor;
        float textSize;
        try {
            this.backgroundColor = a.getColor(R.styleable.DiagramView_background_color, COLOR_DEFAULT_BACKGROUND);
            linesColor = a.getColor(R.styleable.DiagramView_lines_color, COLOR_DEFAULT_LINES);
            axisColor = a.getColor(R.styleable.DiagramView_axis_color, COLOR_DEFAULT_AXIS);
            curveColor = a.getColor(R.styleable.DiagramView_curve_color, COLOR_DEFAULT_CURVE);
            simplifiedCurveColor = a.getColor(R.styleable.DiagramView_simplified_curve_color, COLOR_DEFAULT_SIMPLIFIED_CURVE);
            textColor = a.getColor(R.styleable.DiagramView_curve_color, COLOR_DEFAULT_TEXT);
            textSize = a.getFloat(R.styleable.DiagramView_size_text, SIZE_DEFAULT_TEXT);
        } finally {
            a.recycle();
        }
        this.linesPaint.setColor(linesColor);
        this.axisPaint.setColor(axisColor);
        this.curvePaint.setColor(curveColor);
        this.simplifiedCurvePaint.setColor(simplifiedCurveColor);
        this.textPaint.setColor(textColor);
        this.textPaint.setTextSize(textSize);
    }

    public void setPoints(Point[] points, SimplifiedCurvePoint[] simplifiedCurvePoints){
        this.simplifiedCurvePoints = simplifiedCurvePoints;
        if(simplifiedCurvePoints == null || simplifiedCurvePoints.length == 0) {
            return;
        }

        this.minFrequency = this.maxFrequency = simplifiedCurvePoints[0].frequencyLog10;
        this.minAmplitude = this.maxAmplitude = simplifiedCurvePoints[0].amplitudeDB;

        for(int i = 1; i < simplifiedCurvePoints.length; i ++) {
            if (simplifiedCurvePoints[i].frequencyLog10 > this.maxFrequency)
                this.maxFrequency = simplifiedCurvePoints[i].frequencyLog10;
            if (simplifiedCurvePoints[i].frequencyLog10 < this.minFrequency)
                this.minFrequency = simplifiedCurvePoints[i].frequencyLog10;

            if (simplifiedCurvePoints[i].amplitudeDB > this.maxAmplitude)
                this.maxAmplitude = simplifiedCurvePoints[i].amplitudeDB;
            if (simplifiedCurvePoints[i].amplitudeDB < this.minAmplitude)
                this.minAmplitude = simplifiedCurvePoints[i].amplitudeDB;
        }

        /*
        this.minPhase = this.maxPhase = 0;
        for(int i = 0; i < points.length; i ++){
            if(points[i].phaseDegree > this.maxPhase)
                this.maxPhase = points[i].phaseDegree;
            if(points[i].phaseDegree < this.minPhase)
                this.minPhase = points[i].phaseDegree;
        }
        */

        this.points = points;
        this.maxAmplitude = ((float)Math.ceil(this.maxAmplitude / AMPLITUDE_STEP_DB + 0.5)) * AMPLITUDE_STEP_DB;
        this.minAmplitude = ((float)Math.floor(this.minAmplitude / AMPLITUDE_STEP_DB - 0.5)) * AMPLITUDE_STEP_DB;
    }

    public void redraw(){
        if(points == null)
            return;
        long startTime = System.currentTimeMillis();
        long time = startTime;
        long end;
        end = System.currentTimeMillis();
        Log.d("Time", "Calculation time: " + Long.toString(end - time) + " ms");
        time = end;
        View parent = (View)this.getParent();
        this.getLayoutParams().height = (int)getPhaseY(this.minPhase) + (int)PIXELS_BOTTOM_PADDING;
        this.getLayoutParams().width = (int)getX(this.maxFrequency) + (int)PIXELS_RIGHT_PADDING;
        parent.getLayoutParams().height = (int)getPhaseY(this.minPhase) + (int)PIXELS_BOTTOM_PADDING;
        parent.getLayoutParams().width = (int)getX(this.maxFrequency) + (int)PIXELS_RIGHT_PADDING;
        //parent.requestLayout();
        SurfaceHolder sh = this.getHolder();
        Canvas canvas;
        Log.d("Meanwhile", "Waiting for canvas");
        try {
            Thread.sleep(20);
            while((canvas = sh.lockCanvas()) == null) {
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        end = System.currentTimeMillis();
        Log.d("Time", "Waiten for canvas: " + Long.toString(end - time) + " ms");
        time = end;
        canvas.drawColor(backgroundColor);
        drawAmplitudeVerticals(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Amplitude verticals drawn in " + Long.toString(end - time) + " ms");
        time = end;
        drawAmplitudeHorizontals(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Amplitude horizontals drawn in " + Long.toString(end - time) + " ms");
        time = end;
        drawAmplitudeAxis(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Amplitude axises drawn in " + Long.toString(end - time) + " ms");
        time = end;
        drawPhaseVerticals(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Phase verticals drawn in " + Long.toString(end - time) + " ms");
        time = end;
        drawPhaseHorizontals(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Phase horizontals drawn in " + Long.toString(end - time) + " ms");
        time = end;
        drawPhaseAxis(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Phase axises drawn in " + Long.toString(end - time) + " ms");
        time = end;
        drawCurves(canvas, this.points);
        end = System.currentTimeMillis();
        Log.d("Time", "Curves drawn in " + Long.toString(end - time) + " ms");
        time = end;
        drawSimplifiedCurves(canvas, this.simplifiedCurvePoints);
        end = System.currentTimeMillis();
        Log.d("Time", "Simplified curve drawn in " + Long.toString(end - time) + " ms");
        Log.d("Time", "Total drawing time " + Long.toString(end - startTime) + " ms");
        sh.unlockCanvasAndPost(canvas);
    }

    private void drawSimplifiedCurves(Canvas canvas, SimplifiedCurvePoint[] simplifiedCurvePoints) {
        int i;
        float formerX = getX(simplifiedCurvePoints[0]);
        float formerAmplitudeY = getAmplitudeY(simplifiedCurvePoints[0]);
        float currentX;
        float currentAmplitudeY;

        for(i = 1; i < simplifiedCurvePoints.length; i ++){
            currentX = getX(simplifiedCurvePoints[i]);
            currentAmplitudeY = getAmplitudeY(simplifiedCurvePoints[i]);

            canvas.drawLine(formerX, formerAmplitudeY, currentX, currentAmplitudeY, this.simplifiedCurvePaint);

            formerX = currentX;
            formerAmplitudeY = currentAmplitudeY;
        }
    }

    private void drawAmplitudeHorizontals(Canvas canvas){
        float value;
        float width = getX(this.maxFrequency) - getX(this.minFrequency);
        float[] pts = new float[2*(int)(width/linesLength/2)];

        value = getX(this.minFrequency);
        for(int i = 0; i < pts.length; i ++){
            pts[i] = value += linesLength;
        }

        float current = (int)Math.floor(this.minAmplitude/ AMPLITUDE_STEP_DB)* AMPLITUDE_STEP_DB;
        if(current < this.minAmplitude)
            current += AMPLITUDE_STEP_DB;
        while(current <= this.maxAmplitude) {
            value = getAmplitudeY(current);
            canvas.drawText(Integer.toString((int) current) + "dB", 10, value + textPaint.getTextSize() / 2, textPaint);
            for(int i = 0; i < pts.length; i ++)
                canvas.drawLine(pts[i], value, pts[++i], value, this.linesPaint);
            current += AMPLITUDE_STEP_DB;
        }
    }

    private void drawPhaseHorizontals(Canvas canvas){
        float value;
        float width = getX(this.maxFrequency) - getX(this.minFrequency);
        float[] pts = new float[2*(int)(width/linesLength/2)];

        value = getX(this.minFrequency);
        for(int i = 0; i < pts.length; i ++){
            pts[i] = value += linesLength;
        }

        float current = (int)Math.floor(this.minPhase/ PHASE_STEP_DEGREES)* PHASE_STEP_DEGREES;
        if(current < this.minPhase)
            current += PHASE_STEP_DEGREES;
        while(current <= this.maxPhase) {
            value = getPhaseY(current);
            canvas.drawText(Integer.toString((int)current) + "°", 10, value + textPaint.getTextSize()/2, textPaint);
            for(int i = 0; i < pts.length; i ++)
                canvas.drawLine(pts[i], value, pts[++i], value, this.linesPaint);
            current += PHASE_STEP_DEGREES;
        }
    }

    private void drawAmplitudeVerticals(Canvas canvas){
        float[] decimals = new float[]{0, (float)Math.log10(2), (float)Math.log10(3), (float) Math.log10(4), (float) Math.log10(5)};
        float start = (float)Math.floor(this.minFrequency);
        float decadeStart;
        float value;
        float height = getAmplitudeY(this.minAmplitude) - getAmplitudeY(this.maxAmplitude);
        float[] pts = new float[2*(int)(height/linesLength/2)];

        value = getAmplitudeY(this.maxAmplitude);
        for(int i = 0; i < pts.length; i ++)
            pts[i] = value += linesLength;

        for(float decimal : decimals){
            decadeStart = start;
            if(decadeStart + decimal < this.minFrequency)
                decadeStart++;
            while(decadeStart + decimal <= this.maxFrequency) {
                value = getX(decadeStart + decimal);
                if(decimal == 0){
                    canvas.drawText("10^" + (int)decadeStart, value - 12, getAmplitudeY(this.minAmplitude) + textPaint.getTextSize(), textPaint);
                }
                for(int i = 0; i < pts.length; i ++)
                    canvas.drawLine(value, pts[i], value, pts[++i], this.linesPaint);
                decadeStart++;
            }
        }
    }

    private void drawPhaseVerticals(Canvas canvas){
        float[] decimals = new float[]{0, (float)Math.log10(2), (float)Math.log10(3), (float) Math.log10(4), (float) Math.log10(5)};
        float start = (float)Math.floor(this.minFrequency);
        float decadeStart;
        float value;
        float height = getPhaseY(this.minPhase) - getPhaseY(this.maxPhase);
        float[] pts = new float[2*(int)(height/linesLength/2)];

        value = getPhaseY(this.maxPhase);
        for(int i = 0; i < pts.length; i ++)
            pts[i] = value += linesLength;

        for(float decimal : decimals){
            decadeStart = start;
            if(decadeStart + decimal < this.minFrequency)
                decadeStart++;
            while(decadeStart + decimal <= this.maxFrequency) {
                value = getX(decadeStart + decimal);
                if(decimal == 0){
                    canvas.drawText("10^" + (int)decadeStart, value - 12, getPhaseY(0) + textPaint.getTextSize(), textPaint);
                }
                for(int i = 0; i < pts.length; i ++)
                    canvas.drawLine(value, pts[i], value, pts[++i], this.linesPaint);
                decadeStart++;
            }
        }
    }

    private void drawAmplitudeAxis(Canvas canvas) {
        canvas.drawLine(getX(this.minFrequency), getAmplitudeY(this.minAmplitude), getX(this.maxFrequency), getAmplitudeY(this.minAmplitude), this.axisPaint);
        canvas.drawLine(getX(this.minFrequency), getAmplitudeY(this.maxAmplitude), getX(this.minFrequency), getAmplitudeY(this.minAmplitude), this.axisPaint);
    }

    private void drawPhaseAxis(Canvas canvas) {
        canvas.drawLine(getX(this.minFrequency), getPhaseY(0), getX(this.maxFrequency), getPhaseY(0), this.axisPaint);
        canvas.drawLine(getX(this.minFrequency), getPhaseY(this.maxPhase), getX(this.minFrequency), getPhaseY(this.minPhase), this.axisPaint);
    }

    private void drawCurves(Canvas canvas, Point[] points){
        int i;
        float formerX = getX(points[0]);
        float formerAmplitudeY = getAmplitudeY(points[0]);
        float formerPhaseY = getPhaseY(points[0]);
        float currentX;
        float currentAmplitudeY;
        float currentPhaseY;

        for(i = 1; i < points.length; i ++){
            currentX = getX(points[i]);
            currentAmplitudeY = getAmplitudeY(points[i]);
            currentPhaseY = getPhaseY(points[i]);

            drawLine(canvas, formerX, formerAmplitudeY, currentX, currentAmplitudeY);
            canvas.drawLine(formerX, formerPhaseY, currentX, currentPhaseY, this.curvePaint);

            formerX = currentX;
            formerAmplitudeY = currentAmplitudeY;
            formerPhaseY = currentPhaseY;
        }
    }

    private void drawLine(Canvas canvas, float startX, float startY, float endX, float endY){
        if(startY < PIXELS_TOP_PADDING){
            if(endY <= PIXELS_TOP_PADDING)
                return;
            float ratio = (startY - endY)/(startY - PIXELS_TOP_PADDING);
            startY = PIXELS_TOP_PADDING;
            startX = startX + (endX - startX)*ratio;
        }
        float bottom = getAmplitudeY(this.minAmplitude);
        if(startY > bottom){
            if(endY >= bottom)
                return;
            float ratio = (startY - endY)/(startY - bottom);
            startY = bottom;
            startX = startX + (endX - startX)*ratio;
        }
        if(endY < PIXELS_TOP_PADDING || endY > bottom){
            drawLine(canvas, endX, endY, startX, startY);
            return;
        }
        canvas.drawLine(startX, startY, endX, endY, this.curvePaint);
    }

    private float getX(float frequencyLog10){
        return (frequencyLog10 - this.minFrequency) *PIXELS_PER_DECADE + PIXELS_LEFT_PADDING;
    }

    private float getAmplitudeY(float amplitude){
        return (this.maxAmplitude - amplitude) *PIXELS_PER_DB + PIXELS_TOP_PADDING;
    }

    private float getPhaseY(float phase){
        return getAmplitudeY(this.minAmplitude) + (this.maxPhase - phase) *PIXELS_PER_DEGREE + PIXELS_BETWEEN_DIAGRAMS;
    }

    private float getX(Point point){
        return getX(point.frequencyLog10);
    }

    private float getAmplitudeY(Point point){
        return getAmplitudeY(point.amplitudeDB);
    }

    private float getPhaseY(Point point){
        return getPhaseY(point.phaseDegree);
    }

    private float getX(SimplifiedCurvePoint point){
        return getX(point.frequencyLog10);
    }

    private float getAmplitudeY(SimplifiedCurvePoint point){
        return getAmplitudeY(point.amplitudeDB);
    }
}
