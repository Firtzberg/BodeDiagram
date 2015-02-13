package com.hrca.bode.customs;

import android.content.Context;
import android.util.AttributeSet;

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
}
