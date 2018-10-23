package com.memr.bitcoinspv.ViewModules.Updaters;

import org.bitcoinj.core.PeerGroup;

import java.util.Timer;
import java.util.TimerTask;

import com.memr.bitcoinspv.Adapters.PeerGroupListAdapter;
import com.memr.bitcoinspv.Managers.WalletManager;

public class PeerGroupListUpdater {

    private PeerGroup peerGroup;

    private PeerGroupListAdapter adapter;

    private Timer timer = new Timer();

    public PeerGroupListUpdater(PeerGroup peerGroup, PeerGroupListAdapter adapter) {
        this.peerGroup = peerGroup;
        this.adapter = adapter;
    }

    public void updateListView() {
        adapter.setPeerList(peerGroup.getConnectedPeers());
    }

    public void startPeriodicUpdate() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                WalletManager.runInUIThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        updateListView();
                    }
                });
            }
        }, 0, 1_000);
    }

    public void stopPeriodicUpdate() {
        timer.cancel();
    }
}
