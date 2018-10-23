package com.memr.bitcoinspv.Util;

import android.app.Activity;

import com.memr.bitcoinspv.Models.VeriTransaction;

import org.bitcoinj.kits.WalletAppKit;

import com.memr.bitcoinspv.AmountActivity;
import com.memr.bitcoinspv.DecryptWalletActivity;
import com.memr.bitcoinspv.UnlockActivity;
import com.memr.bitcoinspv.BitcoinSPV;

public class SendHelper {

    private WalletAppKit kit;
    private Activity activity;
    private BitcoinSPV bitcoinSPV;
    private VeriTransaction veriTransaction;

    public SendHelper(WalletAppKit kit, Activity activity, VeriTransaction veriTransaction) {
        this.kit = kit;
        this.activity = activity;
        this.bitcoinSPV = ((BitcoinSPV) activity.getApplication());
        this.veriTransaction = veriTransaction;
    }

    public void startNextActivity(){
        if(isWalletEncrypted()){
            activity.startActivity(DecryptWalletActivity.createIntent(activity, veriTransaction));
        }else if(isLockTransactions() && passwordExist()){
            activity.startActivity(UnlockActivity.createIntent(activity, veriTransaction));
        }else {
            activity.startActivity(AmountActivity.createIntent(activity, veriTransaction));
        }
    }

    private boolean isWalletEncrypted() {
        return kit.wallet().isEncrypted();
    }

    private boolean isLockTransactions() {
        return bitcoinSPV.isLockTransactions();
    }

    private boolean passwordExist(){ return bitcoinSPV.getPasswordManager().doesPasswordExist(); }
}
