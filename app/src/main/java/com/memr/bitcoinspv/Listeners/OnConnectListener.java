package com.memr.bitcoinspv.Listeners;

import org.bitcoinj.kits.WalletAppKit;

public interface OnConnectListener {
    void onSetUpComplete(WalletAppKit kit);

    void onStopAsync();
}
