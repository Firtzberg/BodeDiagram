package com.hrca.bode.bodediagram;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import com.hrca.bode.customs.EditableTransferFunctionView;
import com.hrca.bode.customs.HistoryHelper;
import com.hrca.bode.customs.PolynomialView;
import com.hrca.bode.customs.TransferFunctionView;


public class InputActivity extends Activity {
    public static final String EXTRA_TRANSFER_FUNCTION = "transferFunction";
    public static final String EXTRA_POLYNOMIAL_IDENTIFIER = "polynomialIdentifier";
    public static final String EXTRA_POLYNOMIAL_CHAIN_IDENTIFIER = "polynomialChainIdentifier";
    public static final int REQUEST_CODE_EDIT_POLYNOMIAL = 123;
    public static final int REQUEST_CODE_HISTORY = 234;
    EditableTransferFunctionView transferFunctionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        this.transferFunctionView = (EditableTransferFunctionView) findViewById(R.id.view2);

        this.transferFunctionView.addNumeratorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(InputActivity.this, PolynomialActivity.class);
                i.putExtra(EXTRA_POLYNOMIAL_CHAIN_IDENTIFIER, true);
                startActivityForResult(i, REQUEST_CODE_EDIT_POLYNOMIAL);
            }
        });
        this.transferFunctionView.addDenominatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(InputActivity.this, PolynomialActivity.class);
                i.putExtra(EXTRA_POLYNOMIAL_CHAIN_IDENTIFIER, false);
                startActivityForResult(i, REQUEST_CODE_EDIT_POLYNOMIAL);
            }
        });
        this.transferFunctionView.setOnPolynomialClickListener(new EditableTransferFunctionView.OnPolynomialClickListener() {
            @Override
            public void onPolynomialClick(boolean numerator, int identifier, Parcelable savedInstanceState) {
                Intent i = new Intent();
                i.setClass(InputActivity.this, PolynomialActivity.class);
                i.putExtra(EXTRA_POLYNOMIAL_CHAIN_IDENTIFIER, numerator);
                i.putExtra(EXTRA_POLYNOMIAL_IDENTIFIER, identifier);
                i.putExtra(PolynomialActivity.EXTRA_POLYNOMIAL, savedInstanceState);
                startActivityForResult(i, REQUEST_CODE_EDIT_POLYNOMIAL);
            }
        });
        this.transferFunctionView.setTextSize(20);
}

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(TransferFunctionView.PARCELABLE_TRANSFER_FUNCTION, this.transferFunctionView.onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        this.transferFunctionView.onRestoreInstanceState(savedInstanceState.getParcelable(TransferFunctionView.PARCELABLE_TRANSFER_FUNCTION));
    }

    public void reset(View v){
        this.transferFunctionView.reset();
    }

    public void historyClick(View v){
        startActivityForResult(new Intent(this, HistoryActivity.class), REQUEST_CODE_HISTORY);
    }

    public void bode(View v){
        HistoryHelper.add(this.transferFunctionView);
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra(EXTRA_TRANSFER_FUNCTION, this.transferFunctionView.onSaveInstanceState());
        startActivity(i);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EDIT_POLYNOMIAL) {
            if (resultCode == RESULT_OK && data != null) {
                Parcelable x = data.getParcelableExtra(PolynomialActivity.EXTRA_POLYNOMIAL);
                boolean chainIdentifier = data.getBooleanExtra(EXTRA_POLYNOMIAL_CHAIN_IDENTIFIER, true);
                int identifier = data.getIntExtra(EXTRA_POLYNOMIAL_IDENTIFIER, -1);
                if (x == null)
                    return;
                this.transferFunctionView.updatePolynomial(chainIdentifier, identifier, x);
            }
            return;
        }
        if (requestCode == REQUEST_CODE_HISTORY) {
            if(resultCode == RESULT_OK && data == null){
                this.transferFunctionView.reset();
                return;
            }
            if (resultCode == RESULT_OK && data != null) {
                Parcelable x = data.getParcelableExtra(EXTRA_TRANSFER_FUNCTION);
                if (x != null) {
                    this.transferFunctionView.onRestoreInstanceState(x);
                }
            }
            return;
        }
    }
}
