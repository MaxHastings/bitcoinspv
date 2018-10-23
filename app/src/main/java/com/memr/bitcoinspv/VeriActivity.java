package com.memr.bitcoinspv;

import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public abstract class VeriActivity extends AppCompatActivity {

    private BitcoinSPV bitcoinSPV;

    @Override
    protected void onResume() {
        super.onResume();
        bitcoinSPV = (BitcoinSPV) getApplication();

        if (bitcoinSPV.isSecureWindowEnabled()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        bitcoinSPV = (BitcoinSPV) getApplication();

        if (bitcoinSPV.isSecureWindowEnabled()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

}
