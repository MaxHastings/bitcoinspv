package com.memr.bitcoinspv;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;

import java.net.InetAddress;

public class AddPeerActivity extends WalletAppKitActivity {

    private TextInputLayout hostNameLayout;
    private Button addPeerButton;
    private BitcoinSPV bitcoinSPV;

    public static Intent createIntent(Context context) {
        return new Intent(context, AddPeerActivity.class);
    }

    @Override
    protected void onWalletKitReady() {
        setContentView(com.memr.bitcoinspv.R.layout.activity_add_peer);
        bitcoinSPV = (BitcoinSPV) getApplication();

        hostNameLayout = findViewById(com.memr.bitcoinspv.R.id.hostNameLayout);
        addPeerButton = findViewById(com.memr.bitcoinspv.R.id.addPeerButon);

        addPeerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostNameLayout.setErrorEnabled(false);
                String hostName = hostNameLayout.getEditText().getText().toString();
                if (hostName.isEmpty()) {
                    hostNameLayout.setError(getString(com.memr.bitcoinspv.R.string.invalid_input));
                } else {
                    try {
                        InetAddress.getByName(hostName); //Check to see if hostName is valid
                        bitcoinSPV.getPeerManager().addPeerAddress(hostName);
                        setResult(RESULT_OK);
                        finish();
                    } catch (Exception e) {
                        hostNameLayout.setError(getString(com.memr.bitcoinspv.R.string.invalid_input));
                    }
                }
            }
        });
    }

    @Override
    protected void onWalletKitStop() {
        addPeerButton.setOnClickListener(null);
    }
}
