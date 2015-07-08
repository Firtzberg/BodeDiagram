package com.hrca.bode.customs;

import android.content.Context;
import android.util.AttributeSet;

import org.ejml.data.Complex64F;

import java.util.ArrayList;

/**
 * Created by Hrvoje on 18.1.2015..
 */
public class PolynomialView extends PolynomialBaseView<PolynomialElementView> {
    private static ArrayList<PolynomialView> recycleList = new ArrayList<>();
    private PolynomialView(Context context) {
        super(context);
    }

    private PolynomialView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected PolynomialElementView getGeneric() {
        return PolynomialElementView.getUnused(getContext());
    }

    public void recycle(){
        recycleList.add(this);
    }

    public static PolynomialView getUnused(Context context){
        PolynomialView polynomialView;
        if(recycleList.size() > 0){
            polynomialView = recycleList.get(recycleList.size() - 1);
            recycleList.remove(recycleList.size() - 1);
            return polynomialView;
        }
        polynomialView = new PolynomialView(context);
        return polynomialView;
    }

    public void setRoot(Complex64F root){
        PolynomialElementView element;
        int elements;
        if(root.isReal())
            elements = 2;
        else elements = 3;
        while(this.list.size() > elements)
            this.remove();
        while(this.list.size() < elements)
            this.add();

        element = this.list.get(0);
        element.setShowSign(true);
        element.setNumerator(1);
        element.setDenominator(1);

        if(root.isReal()){
            element = this.list.get(1);
            element.setShowSign(false);
            element.setNumerator(1);
            element.setDenominator((float)-root.getReal());
        }
        else {
            float wn = (float)root.getMagnitude();
            float zeta = (float)root.real/wn + 1.0F;
            zeta -= 1.0;
            element = this.list.get(1);
            element.setShowSign(true);
            element.setNumerator(-2 * zeta);
            element.setDenominator(wn);

            element = this.list.get(2);
            element.setShowSign(false);
            element.setNumerator(1);
            element.setDenominator(wn*wn);
        }
    }
}
