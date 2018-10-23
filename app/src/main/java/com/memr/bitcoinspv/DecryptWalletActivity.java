package com.memr.bitcoinspv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;

import com.memr.bitcoinspv.Managers.PasswordManager;
import com.memr.bitcoinspv.Models.VeriTransaction;


public class DecryptWalletActivity extends VeriActivity {

    private final static String VERI_TRANSACTION = "veriTransaction";
    private TextInputLayout passwordLayout;
    private Button unlockButton;
    private VeriTransaction veriTransaction;
    private PasswordManager passwordManager;

    public static Intent createIntent(Context context, VeriTransaction veriTransaction) {
        Intent intent = new Intent(context, DecryptWalletActivity.class);
        intent.putExtra(VERI_TRANSACTION, veriTransaction);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.memr.bitcoinspv.R.layout.activity_decrypt_wallet);
        passwordManager = ((BitcoinSPV) getApplication()).getPasswordManager();

        veriTransaction = (VeriTransaction) getIntent().getSerializableExtra(VERI_TRANSACTION);

        unlockButton = findViewById(com.memr.bitcoinspv.R.id.unlockButton);

        passwordLayout = findViewById(com.memr.bitcoinspv.R.id.passwordLayout);

        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordCorrect()) {
                    veriTransaction.setPassword(getPassword());
                    startActivity(AmountActivity.createIntent(DecryptWalletActivity.this, veriTransaction));
                    finish(); //Prevent app from going back to this activity after its finished.
                } else {
                    passwordLayout.setError(getString(com.memr.bitcoinspv.R.string.password_is_incorrect));
                }
            }
        });

    }

    public String getPassword() {
        return passwordLayout.getEditText().getText().toString();
    }

    public boolean isPasswordCorrect() {
        return passwordManager.checkPassword(getPassword());
    }

}
