package com.hrca.bode.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hrca.bode.bodediagram.R;

import org.ejml.data.Complex64F;


/**
 * TODO: document your custom view class.
 */
public class TransferFunctionView extends HorizontalScrollView {

    public static final String PARCELABLE_GAIN_KEY = "gain";
    public static final String PARCELABLE_ASTATISM_KEY = "astatismValue";
    public static final String PARCELABLE_NUMERATOR_KEY = "numeratorChain";
    public static final String PARCELABLE_DENOMINATOR_KEY = "denominatorChain";
    public static final String PARCELABLE_TRANSFER_FUNCTION = "transferFunction";

    protected final TextView gainView;
    protected final AstatismView astatismView;
    protected final PolynomialChainView numeratorChainView;
    protected final PolynomialChainView denominatorChainView;
    private final RelativeLayout container;

    public TransferFunctionView(Context context) {
        this(context, null);
    }

    public TransferFunctionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(getContext(), R.layout.view_transfer_function, this);
        this.gainView = (TextView)findViewById(R.id.gain);
        this.astatismView = (AstatismView)findViewById(R.id.astatism);
        this.numeratorChainView = (PolynomialChainView)findViewById(R.id.numerator_chain);
        this.denominatorChainView = (PolynomialChainView)findViewById(R.id.denominator_chain);
        this.container = (RelativeLayout)findViewById(R.id.tf_container);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.TransferFunctionView, 0, 0);
        float gain = 1;
        int astatism = -1;
        try {
            gain = a.getInteger(R.styleable.TransferFunctionView_gain, 1);
            astatism = a.getInt(R.styleable.TransferFunctionView_tf_astatism, -1);
        } finally {
            a.recycle();
        }
        this.gainView.setText(Float.toString(gain));
        this.astatismView.setAstatism(astatism);
        this.setTextSize(20);
    }

    public int getAstatism(){
        return this.astatismView.getAstatism();
    }

    public void setAstatism(int astatism){
        this.astatismView.setAstatism(astatism);
    }

    public double getGain(){
        double gain = 1;
        try {
            gain = Double.parseDouble(this.gainView.getText().toString());
        }
        catch (NumberFormatException nfe){
            nfe.printStackTrace();
        }
        return gain;
    }

    public void setGain(double gain){
        this.gainView.setText(Double.toString(gain));
    }

    public void addNumeratorRoots(Complex64F[] roots){
        this.numeratorChainView.addRoots(roots);
    }

    public void addDenominatorRoots(Complex64F[] roots){
        this.denominatorChainView.addRoots(roots);
    }

    public double[][] getNumeratorCoefficientArrays(){
        return this.numeratorChainView.getCoefficientArrays();
    }

    public double[][] getDenominatorCoefficientArrays(){
        return this.denominatorChainView.getCoefficientArrays();
    }

    public void setTextSize(float size){
        this.gainView.setTextSize(size);
        this.astatismView.setTextSize(size);
        this.numeratorChainView.setTextSize(size);
        this.denominatorChainView.setTextSize(size);
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener){
        this.container.setOnClickListener(onClickListener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener onLongClickListener){
        this.container.setOnLongClickListener(onLongClickListener);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putDouble(PARCELABLE_GAIN_KEY, this.getGain());
        bundle.putParcelable(PARCELABLE_ASTATISM_KEY, this.astatismView.onSaveInstanceState());
        bundle.putParcelable(PARCELABLE_NUMERATOR_KEY, this.numeratorChainView.onSaveInstanceState());
        bundle.putParcelable(PARCELABLE_DENOMINATOR_KEY, this.denominatorChainView.onSaveInstanceState());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.setGain(bundle.getDouble(PARCELABLE_GAIN_KEY));
            this.astatismView.onRestoreInstanceState(bundle.getParcelable(PARCELABLE_ASTATISM_KEY));
            this.numeratorChainView.onRestoreInstanceState(bundle.getParcelable(PARCELABLE_NUMERATOR_KEY));
            this.denominatorChainView.onRestoreInstanceState(bundle.getParcelable(PARCELABLE_DENOMINATOR_KEY));
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }
}
