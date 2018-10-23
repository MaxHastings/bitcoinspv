package com.memr.bitcoinspv;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.memr.bitcoinspv.Dialogs.ConfirmSendDialog;
import com.memr.bitcoinspv.Managers.ExchangeManager;
import com.memr.bitcoinspv.Models.VeriTransaction;
import com.memr.bitcoinspv.Util.UtilMethods;

import org.bitcoinj.core.Coin;
import org.bitcoinj.utils.Fiat;

import static android.view.View.GONE;

public class ReviewActivity extends WalletAppKitActivity implements ConfirmSendDialog.OnClickListener {

    private final static String VERI_TRANSACTION = "veriTransaction";
    private final static String CONFIRM_DIALOG_TAG = "confirmDialog";
    private BitcoinSPV bitcoinSPV;
    private ExchangeManager exchangeManager;
    private Button sendButton;
    private ProgressBar progressBar;
    private TextView totalView;
    private TextView feeView;
    private TextView amountView;
    private TextView addrView;
    private TextView contactView;

    private TextView totalFiatView;
    private TextView amountFiatView;
    private TextView feeFiatView;

    private VeriTransaction veriTransaction;

    public static Intent createIntent(Context context, VeriTransaction veriTransaction) {
        Intent intent = new Intent(context, ReviewActivity.class);
        intent.putExtra(VERI_TRANSACTION, veriTransaction);
        return intent;
    }

    @Override
    protected void onWalletKitStop() {
        sendButton.setOnClickListener(null);
    }

    @Override
    protected void onWalletKitReady() {
        setContentView(com.memr.bitcoinspv.R.layout.activity_transaction_review);
        bitcoinSPV = (BitcoinSPV) getApplication();
        exchangeManager = bitcoinSPV.getExchangeManager();

        veriTransaction = (VeriTransaction) getIntent().getSerializableExtra(VERI_TRANSACTION);

        totalView = findViewById(com.memr.bitcoinspv.R.id.totalAmount);
        feeView = findViewById(com.memr.bitcoinspv.R.id.fee);
        amountView = findViewById(com.memr.bitcoinspv.R.id.amount);
        addrView = findViewById(com.memr.bitcoinspv.R.id.sendAddr);
        progressBar = findViewById(com.memr.bitcoinspv.R.id.progressBar);
        sendButton = findViewById(com.memr.bitcoinspv.R.id.sendButton);
        contactView = findViewById(com.memr.bitcoinspv.R.id.contactName);
        totalFiatView = findViewById(com.memr.bitcoinspv.R.id.totalFiat);
        feeFiatView = findViewById(com.memr.bitcoinspv.R.id.feeFiat);
        amountFiatView = findViewById(com.memr.bitcoinspv.R.id.amountFiat);

        progressBar.setVisibility(GONE);

        try {
            Coin total = veriTransaction.getTotal();
            Coin amount = veriTransaction.getAmount();
            Coin fee = veriTransaction.getFee();
            amountView.setText(amount.toFriendlyString());
            feeView.setText(fee.toFriendlyString());
            totalView.setText(total.toFriendlyString());
            addrView.setText(veriTransaction.getContact().getAddress());

            Fiat fiatTotal = exchangeManager.getExchangeRate().coinToFiat(total);
            Fiat fiatAmount = exchangeManager.getExchangeRate().coinToFiat(amount);
            Fiat fiatFee = exchangeManager.getExchangeRate().coinToFiat(fee);

            totalFiatView.setText(UtilMethods.roundFiat(fiatTotal).toFriendlyString());
            amountFiatView.setText(UtilMethods.roundFiat(fiatAmount).toFriendlyString());
            feeFiatView.setText(UtilMethods.roundFiat(fiatFee).toFriendlyString());

            String name = veriTransaction.getContact().getName();
            if(name == null || name.isEmpty()){
                contactView.setVisibility(GONE);
            }else{
                contactView.setText(name);
            }
        } catch (Exception e) {
            Toast.makeText(ReviewActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createConfirmationDialog();
            }
        });
    }

    private void createConfirmationDialog(){
        ConfirmSendDialog confirmSendDialog = new ConfirmSendDialog();
        confirmSendDialog.setArguments(ConfirmSendDialog.createBundle(veriTransaction));
        confirmSendDialog.setListener(this);
        confirmSendDialog.show(getSupportFragmentManager(), CONFIRM_DIALOG_TAG);
    }

    @Override
    public void OnConfirm() {
        startActivity(ProcessTransactionActivity.createIntent(ReviewActivity.this, veriTransaction));
    }
}
