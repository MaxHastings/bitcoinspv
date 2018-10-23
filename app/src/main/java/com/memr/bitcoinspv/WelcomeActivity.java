package com.memr.bitcoinspv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.bitcoinj.wallet.Wallet;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class WelcomeActivity extends AppCompatActivity {

    private final static int READ_REQUEST_CODE = 65;
    private Button getStartedButton;
    private Button importWalletButton;
    private Button importSeedButton;
    private ProgressBar progressBar;

    public static Intent createIntent(Context context) {
        return new Intent(context, WelcomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.memr.bitcoinspv.R.layout.activity_welcome);

        progressBar = findViewById(com.memr.bitcoinspv.R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        getStartedButton = findViewById(com.memr.bitcoinspv.R.id.getStartedButton);
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(VericoinGettingStartedActivity.createIntent(WelcomeActivity.this));
            }
        });

        importWalletButton = findViewById(com.memr.bitcoinspv.R.id.importWalletButton);
        importWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFile();
            }
        });

        importSeedButton = findViewById(com.memr.bitcoinspv.R.id.importSeedButton);
        importSeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ImportSeedActivity.createIntent(WelcomeActivity.this));
            }
        });
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Open a file with the requested MIME type.
        intent.setType("application/x-bitcoin");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private void loadingWalletFailed(String error) {
        getStartedButton.setEnabled(true);
        importWalletButton.setEnabled(true);
        importSeedButton.setEnabled(true);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
    }

    private void loadingWallet() {
        getStartedButton.setEnabled(false);
        importWalletButton.setEnabled(false);
        importSeedButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void loadComplete() {
        getStartedButton.setEnabled(true);
        importWalletButton.setEnabled(true);
        importSeedButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData != null) {
                final Uri uri = resultData.getData();
                loadingWallet();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Wallet importWallet = Wallet.loadFromFileStream(getContentResolver().openInputStream(uri));
                            if (importWallet.isEncrypted()) {
                                startActivity(SetUpEncryptedWallet.createIntent(WelcomeActivity.this, uri));
                            } else {
                                startActivity(SetUpDecryptedWalletWithPassword.createIntent(WelcomeActivity.this, uri));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadComplete();
                                }
                            });
                        } catch (final Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingWalletFailed(e.toString());
                                }
                            });
                        }
                    }
                }).start();
            }
        }
    }
}
