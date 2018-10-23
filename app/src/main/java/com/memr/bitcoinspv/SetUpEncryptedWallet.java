package com.memr.bitcoinspv;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.memr.bitcoinspv.Managers.WalletManager;

import org.bitcoinj.wallet.Wallet;

public class SetUpEncryptedWallet extends VeriActivity {

    private final static String URI_EXTRA = "uri";

    private ProgressBar progressBar;

    private TextInputLayout passwordLayout;

    private Button importButton;

    private Uri uri;

    private WalletManager walletManager;

    public static Intent createIntent(Context context, Uri uri) {
        return new Intent(context, SetUpEncryptedWallet.class).putExtra(URI_EXTRA, uri);
    }

    @Override
    protected void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        setContentView(com.memr.bitcoinspv.R.layout.activity_set_up_encrypted_wallet);

        walletManager = ((BitcoinSPV) getApplication()).getWalletManager();

        uri = getIntent().getParcelableExtra(URI_EXTRA);

        try {
            final Wallet importWallet = Wallet.loadFromFileStream(getContentResolver().openInputStream(uri));

            passwordLayout = findViewById(com.memr.bitcoinspv.R.id.walletPasswordInputLayout);
            progressBar = findViewById(com.memr.bitcoinspv.R.id.progressBar);
            progressBar.setVisibility(View.GONE);
            importButton = findViewById(com.memr.bitcoinspv.R.id.setPasswordButton);

            importButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resetError();
                    checkPassword(importWallet, getPassword());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void checkPassword(final Wallet wallet, final String password) {
        importing();
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isPasswordCorrect = wallet.checkPassword(password);
                try {
                    if (isPasswordCorrect) {
                        walletManager.createWalletFromFile(SetUpEncryptedWallet.this, uri, password, false); //We don't encrypt a wallet that is already encrypted.
                        importComplete();
                    } else {
                        setError(getString(com.memr.bitcoinspv.R.string.password_is_incorrect));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setError(e.toString());
                }
            }
        }).start();
    }

    public void resetError() {
        passwordLayout.setErrorEnabled(false);
    }

    public void importing() {
        progressBar.setVisibility(View.VISIBLE);
        importButton.setText("");
    }

    public void importComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SetUpEncryptedWallet.this, com.memr.bitcoinspv.R.string.wallet_imported, Toast.LENGTH_LONG).show();
                startActivity(SplashActivity.createIntent(SetUpEncryptedWallet.this, false));
            }
        });
    }

    public void setError(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                passwordLayout.setError(error);
                importButton.setText(getString(com.memr.bitcoinspv.R.string.import_button));
            }
        });
    }

    public String getPassword() {
        return passwordLayout.getEditText().getText().toString();
    }

}
