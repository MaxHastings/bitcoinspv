package com.memr.bitcoinspv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;

import com.memr.bitcoinspv.Util.UtilMethods;

import java.util.ArrayList;

public class ImportSeedActivity extends VeriActivity {

    private TextInputLayout seedLayout;
    private TextInputLayout creationTimeLayout;
    private Button importButton;

    public static Intent createIntent(Context context) {
        return new Intent(context, ImportSeedActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.memr.bitcoinspv.R.layout.activity_import_seed);

        seedLayout = findViewById(com.memr.bitcoinspv.R.id.seedLayout);
        creationTimeLayout = findViewById(com.memr.bitcoinspv.R.id.creationTimeLayout);
        importButton = findViewById(com.memr.bitcoinspv.R.id.importButton);

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seedLayout.setErrorEnabled(false);
                creationTimeLayout.setErrorEnabled(false);
                try {
                    long creationTime = getCreationTime();
                    try {
                        ArrayList<String> mnemonicList = new ArrayList<>(UtilMethods.stringToMnemonic(getSeed()));
                        startActivity(SetUpSeedWalletWithPassword.createIntent(ImportSeedActivity.this, mnemonicList, creationTime));
                    } catch (Exception e) {
                        e.printStackTrace();
                        seedLayout.setError(getString(com.memr.bitcoinspv.R.string.invalid_input));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    creationTimeLayout.setError(getString(com.memr.bitcoinspv.R.string.invalid_input));
                }
            }
        });
    }

    public String getSeed() {
        return seedLayout.getEditText().getText().toString();
    }

    public long getCreationTime() {
        String creationString = creationTimeLayout.getEditText().getText().toString();
        if (creationString.isEmpty()) {
            return 0;
        } else {
            return Long.parseLong(creationTimeLayout.getEditText().getText().toString());
        }
    }
}
