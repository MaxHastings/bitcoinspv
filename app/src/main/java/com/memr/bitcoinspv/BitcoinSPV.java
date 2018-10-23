package com.memr.bitcoinspv;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;

import com.memr.bitcoinspv.Listeners.OnConnectListener;
import com.memr.bitcoinspv.Managers.ContactManager;
import com.memr.bitcoinspv.Managers.CustomPeerManager;
import com.memr.bitcoinspv.Managers.ExchangeManager;
import com.memr.bitcoinspv.Managers.PasswordManager;
import com.memr.bitcoinspv.Managers.VeriNotificationManager;
import com.memr.bitcoinspv.Managers.WalletManager;
import com.memr.bitcoinspv.Util.UtilMethods;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

public class BitcoinSPV extends MultiDexApplication implements OnConnectListener, WalletCoinsReceivedEventListener{

    private final static String PREFERENCE_FILE_KEY = "info.vericoin.verimobile.PREFERENCE_FILE_KEY";

    private SharedPreferences sharedPref;

    private SharedPreferences defaultPref;

    private CustomPeerManager peerManager;

    private WalletManager walletManager;

    private PasswordManager passwordManager;

    private ContactManager contactManager;

    private ExchangeManager exchangeManager;

    private VeriNotificationManager veriNotificationManager;

    private WalletAppKit kit;

    public ContactManager getContactManager() {
        return contactManager;
    }

    public CustomPeerManager getPeerManager() {
        return peerManager;
    }

    public WalletManager getWalletManager() {
        return walletManager;
    }

    public PasswordManager getPasswordManager() {
        return passwordManager;
    }

    public ExchangeManager getExchangeManager() {
        return exchangeManager;
    }

    public VeriNotificationManager getVeriNotificationManager() {
        return veriNotificationManager;
    }

    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();

        BriefLogFormatter.init();

        sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        defaultPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        if(passwordManager == null) {
            passwordManager = new PasswordManager(sharedPref);
        }
        if(walletManager == null) {
            walletManager = new WalletManager(this);
            walletManager.addConnectListener(this);
        }
        if(peerManager == null) {
            peerManager = new CustomPeerManager(sharedPref, walletManager);
        }
        if(contactManager == null){
            contactManager = new ContactManager(sharedPref);
        }
        if(exchangeManager == null){
            exchangeManager = new ExchangeManager(sharedPref, defaultPref);
            exchangeManager.downloadExchangeRateList(this);
        }
        if(veriNotificationManager == null){
            veriNotificationManager = new VeriNotificationManager();
            veriNotificationManager.createNotificationChannel(this);
        }

        UtilMethods.setContext(this);

        if (walletManager.doesWalletExist(this)) {
            walletManager.startWallet(this);
        }
    }

    public boolean isLockTransactions() {
        return defaultPref.getBoolean(getString(com.memr.bitcoinspv.R.string.lock_transactions_key), false);
    }

    public boolean isFingerPrintEnabled() {
        return defaultPref.getBoolean(getString(com.memr.bitcoinspv.R.string.fingerprint_enabled_key), true);
    }

    public boolean isSecureWindowEnabled() {
        return defaultPref.getBoolean(getString(com.memr.bitcoinspv.R.string.secure_window_key), true);
    }

    @Override
    public void onSetUpComplete(WalletAppKit kit) {
        this.kit = kit;
        kit.wallet().addCoinsReceivedEventListener(WalletManager.runInUIThread, this);
    }

    @Override
    public void onStopAsync() {
        kit.wallet().removeCoinsReceivedEventListener(this);
    }

    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        veriNotificationManager.createNotification(BitcoinSPV.this, kit.wallet(), tx);
    }
}
