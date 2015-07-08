package com.hrca.bode.customs;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Hrvoje on 17.1.2015..
 */
public class PolynomialElementView extends PolynomialElementBaseView<TextView> {

    private static ArrayList<PolynomialElementBaseView<TextView>> recycleList = new ArrayList<>();

    private PolynomialElementView(Context context) {
        this(context, null);
    }

    private PolynomialElementView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs){
        this.reposition();
    }

    protected void reposition(){
        reSign();

        if(this.denominator == 1){
            this.fractalView.removeView(this.denominatorView);
            this.fractalView.removeView(this.fractionBarView);

        }
        else{
            this.denominatorView.setText(Float.toString((float)this.denominator));
            if(this.fractionBarView.getParent() == null)
                this.fractalView.addView(this.fractionBarView);
            if(this.denominatorView.getParent() == null)
                this.fractalView.addView(this.denominatorView);
        }

        if(this.denominator == 1 && this.numerator == 1 && this.getExponent() != 0)
            this.numeratorView.setText("");
        else this.numeratorView.setText(Float.toString((float)this.numerator));

        if(this.getExponent() == 0)
            this.removeView(this.exponentView);
        else if(this.exponentView.getParent() == null)
            this.addView(this.exponentView);
    }

    @Override
    public void setNumerator(double numerator) {
        if((this.numerator == 0) ^ (numerator == 0)){
            this.setVisibility(numerator == 0 ? GONE : VISIBLE);
        }
        if(this.sign ^ (numerator >= 0)) {
            this.sign = !this.sign;
            reSign();
        }
        if(!this.sign)
            numerator = -numerator;
        if(this.numerator == numerator)
            return;
        this.numerator = numerator;
        if(this.numerator == 1 && this.denominator == 1 && this.getExponent() != 0) {
            this.numeratorView.setText("");
        }
        else{
            this.numeratorView.setText(Float.toString((float)numerator));
        }
    }

    @Override
    public void setDenominator(double denominator) {
        if(denominator == 0)
            denominator = 1;
        if(this.denominator == denominator)
            return;
        if(this.denominator == 1 || denominator == 1) {
            this.denominator = denominator;
            this.reposition();
        }
        else{
            this.denominator = denominator;
            this.denominatorView.setText(Float.toString((float)denominator));
        }
    }

    @Override
    public void setExponent(int exponent) {
        if(this.getExponent() == exponent)
            return;
        if((this.getExponent() == 0) ^ (exponent == 0)){
            this.exponentView.setExponent(exponent);
            this.reposition();
        }
        else{
            this.exponentView.setExponent(exponent);
        }
    }

    @Override
    protected TextView getGeneric(Context context) {
        return new TextView(context);
    }

    @Override
    protected ArrayList<PolynomialElementBaseView<TextView>> getRecycleList() {
        return recycleList;
    }

    public static PolynomialElementView getUnused(Context context){
        PolynomialElementView pev;
        if(recycleList.size() > 0){
            pev = (PolynomialElementView)recycleList.get(recycleList.size() - 1);
            recycleList.remove(recycleList.size() - 1);
            return pev;
        }
        pev = new PolynomialElementView(context);
        return pev;
    }
}
