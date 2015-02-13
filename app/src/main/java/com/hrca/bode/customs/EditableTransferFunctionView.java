package com.hrca.bode.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hrca.bode.bodediagram.R;

/**
 * TODO: document your custom view class.
 */
public class EditableTransferFunctionView extends RelativeLayout {

    final TextView gainView;
    public final AstatismView astatismView;
    public final PolynomialChainView numeratorChainView;
    public final PolynomialChainView denominatorChainView;
    final ImageButton arrowUp;
    final ImageButton arrowDown;
    public final Button addNumeratorButton;
    public final Button addDenominatorButton;

    public EditableTransferFunctionView(Context context) {
        this(context, null);
    }

    public EditableTransferFunctionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(getContext(), R.layout.view_editable_transfer_function, this);
        this.gainView = (TextView)findViewById(R.id.gain);
        this.astatismView = (AstatismView)findViewById(R.id.astatism);
        this.numeratorChainView = (PolynomialChainView)findViewById(R.id.numerator_chain);
        this.denominatorChainView = (PolynomialChainView)findViewById(R.id.denominator_chain);
        this.arrowUp = (ImageButton)findViewById(R.id.arrow_up);
        this.arrowDown = (ImageButton)findViewById(R.id.arrow_down);
        this.addNumeratorButton = (Button)findViewById(R.id.add_num);
        this.addDenominatorButton = (Button)findViewById(R.id.add_denom);

        this.arrowUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                astatismView.up();
            }
        });
        this.arrowDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                astatismView.down();
            }
        });
        this.numeratorChainView.setOnPolynomialLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                numeratorChainView.remove((PolynomialView) v);
                return true;
            }
        });
        this.denominatorChainView.setOnPolynomialLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                denominatorChainView.remove((PolynomialView) v);
                return true;
            }
        });
        this.numeratorChainView.setOnPolynomialClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(v instanceof PolynomialView))
                    return;
                if(onPolynomialClickListener == null)
                    return;
                onPolynomialClickListener.onPolynomialClick(true,
                        numeratorChainView.list.indexOf(v), ((PolynomialView)v).onSaveInstanceState());
            }
        });
        this.denominatorChainView.setOnPolynomialClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(v instanceof PolynomialView))
                    return;
                if(onPolynomialClickListener == null)
                    return;
                onPolynomialClickListener.onPolynomialClick(false,
                        denominatorChainView.list.indexOf(v), ((PolynomialView)v).onSaveInstanceState());
            }
        });
        init(attrs);
    }

    public abstract interface OnPolynomialClickListener{
        public void onPolynomialClick(boolean numerator, int identifier, Parcelable savedInstanceState);
    }
    protected OnPolynomialClickListener onPolynomialClickListener = null;
    public void setOnPolynomialClickListener(OnPolynomialClickListener l){
        this.onPolynomialClickListener = l;
    }

    private void init(AttributeSet attrs) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.EditableTransferFunctionView, 0, 0);
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

    public void setTextSize(float size){
        this.gainView.setTextSize(size);
        this.astatismView.setTextSize(size);
        this.numeratorChainView.setTextSize(size);
        this.denominatorChainView.setTextSize(size);
    }

    public void updatePolynomial(boolean chainIdentifier, int identifier, Parcelable x) {
        PolynomialChainView polynomialChainView;
        if(chainIdentifier)
            polynomialChainView = this.numeratorChainView;
        else polynomialChainView = this.denominatorChainView;
        polynomialChainView.updatePolynomial(identifier, x);
    }

    public void reset(){
        this.gainView.setText("1.0");
        this.astatismView.setAstatism(0);
        this.numeratorChainView.reset();
        this.denominatorChainView.reset();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putDouble(TransferFunctionView.PARCELABLE_GAIN_KEY, this.getGain());
        bundle.putParcelable(TransferFunctionView.PARCELABLE_ASTATISM_KEY, this.astatismView.onSaveInstanceState());
        bundle.putParcelable(TransferFunctionView.PARCELABLE_NUMERATOR_KEY, this.numeratorChainView.onSaveInstanceState());
        bundle.putParcelable(TransferFunctionView.PARCELABLE_DENOMINATOR_KEY, this.denominatorChainView.onSaveInstanceState());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.gainView.setText(Double.toString(bundle.getDouble(TransferFunctionView.PARCELABLE_GAIN_KEY)));
            this.astatismView.onRestoreInstanceState(bundle.getParcelable(TransferFunctionView.PARCELABLE_ASTATISM_KEY));
            this.numeratorChainView.onRestoreInstanceState(bundle.getParcelable(TransferFunctionView.PARCELABLE_NUMERATOR_KEY));
            this.denominatorChainView.onRestoreInstanceState(bundle.getParcelable(TransferFunctionView.PARCELABLE_DENOMINATOR_KEY));
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }
}
