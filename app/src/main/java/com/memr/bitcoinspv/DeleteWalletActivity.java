package com.memr.bitcoinspv;

import android.content.Context;
import android.content.Intent;

import com.memr.bitcoinspv.Managers.WalletManager;

public class DeleteWalletActivity extends WalletAppKitActivity {

    private WalletManager walletManager;

    public static Intent createIntent(Context context) {
        return new Intent(context, DeleteWalletActivity.class);
    }

    @Override
    protected void onWalletKitStop() {

    }

    @Override
    protected void onWalletKitReady() {
        setContentView(com.memr.bitcoinspv.R.layout.activity_delete_wallet);
        walletManager = ((BitcoinSPV) getApplication()).getWalletManager();
        walletManager.deleteWallet(this);
    }

    @Override
    public void onBackPressed() {
        //Do nothing. (We don't want user to go back while a transaction is being processed.)
    }

}
