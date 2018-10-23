package com.memr.bitcoinspv;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.memr.bitcoinspv.Managers.WalletManager;
import com.memr.bitcoinspv.ViewModules.NewPasswordValidation;

import javax.annotation.Nullable;

public abstract class SetUpWalletWithPassword extends VeriActivity {

    protected WalletManager walletManager;
    private ProgressBar progressBar;
    private TextInputLayout passwordLayout;

    private TextInputLayout rePasswordLayout;

    private Button importWalletButton;

    private CheckBox encryptWallet;

    private CheckBox noPassword;

    private NewPasswordValidation newPasswordValidation;

    @Override
    protected void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        setContentView(com.memr.bitcoinspv.R.layout.activity_set_up_wallet);
        walletManager = ((BitcoinSPV) getApplication()).getWalletManager();

        progressBar = findViewById(com.memr.bitcoinspv.R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        passwordLayout = findViewById(com.memr.bitcoinspv.R.id.passwordInputLayout);
        rePasswordLayout = findViewById(com.memr.bitcoinspv.R.id.rePasswordInputLayout);
        importWalletButton = findViewById(com.memr.bitcoinspv.R.id.importWalletButton);
        encryptWallet = findViewById(com.memr.bitcoinspv.R.id.encryptWallet);
        noPassword = findViewById(com.memr.bitcoinspv.R.id.noPasswordBox);

        newPasswordValidation = new NewPasswordValidation(passwordLayout, rePasswordLayout);

        importWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetError();
                newPasswordValidation.resetErrors();
                if (isNoPasswordChecked()) {
                    importWallet(null);
                } else if (newPasswordValidation.checkValidity()) {
                    importWallet(newPasswordValidation.getPassword());
                }
            }
        });

        noPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    passwordLayout.setEnabled(false);
                    rePasswordLayout.setEnabled(false);
                    encryptWallet.setEnabled(false);
                } else {
                    passwordLayout.setEnabled(true);
                    rePasswordLayout.setEnabled(true);
                    encryptWallet.setEnabled(true);
                }
            }
        });

    }

    abstract void importWallet(@Nullable String password);

    public void resetError() {
        passwordLayout.setErrorEnabled(false);
        rePasswordLayout.setErrorEnabled(false);
    }

    public void importing() {
        progressBar.setVisibility(View.VISIBLE);
        importWalletButton.setText("");
        importWalletButton.setEnabled(false);
    }

    public void importFailed(String error) {
        progressBar.setVisibility(View.GONE);
        importWalletButton.setText(com.memr.bitcoinspv.R.string.import_button);
        importWalletButton.setEnabled(true);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    public void importComplete() {
        Toast.makeText(this, com.memr.bitcoinspv.R.string.wallet_imported, Toast.LENGTH_LONG).show();
        startActivity(SplashActivity.createIntent(SetUpWalletWithPassword.this, false));
    }

    public boolean isEncryptWallet() {
        return encryptWallet.isChecked();
    }

    public boolean isNoPasswordChecked() {
        return noPassword.isChecked();
    }


}
