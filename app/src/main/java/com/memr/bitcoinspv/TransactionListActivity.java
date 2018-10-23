package com.memr.bitcoinspv;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.memr.bitcoinspv.Adapters.TransactionListAdapter;
import com.memr.bitcoinspv.Managers.ExchangeManager;
import com.memr.bitcoinspv.Managers.VeriNotificationManager;
import com.memr.bitcoinspv.Managers.WalletManager;
import com.memr.bitcoinspv.Util.RecyclerViewEmptySupport;
import com.memr.bitcoinspv.ViewModules.Updaters.TransactionListUpdater;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.utils.ExchangeRate;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

public class TransactionListActivity extends WalletAppKitActivity implements ExchangeManager.OnExchangeRateChange, WalletCoinsReceivedEventListener {

    private RecyclerViewEmptySupport mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private TransactionListAdapter mAdapter;

    private TransactionListUpdater transactionListUpdater;

    private TextView emptyTextView;

    private VeriNotificationManager veriNotificationManager;

    public static Intent createIntent(Context context) {
        return new Intent(context, TransactionListActivity.class);
    }

    private BitcoinSPV bitcoinSPV;
    private ExchangeManager exchangeManager;

    @Override
    protected void onWalletKitReady() {
        setContentView(com.memr.bitcoinspv.R.layout.recycler_view);

        bitcoinSPV = (BitcoinSPV) getApplication();
        exchangeManager = bitcoinSPV.getExchangeManager();
        exchangeManager.addExchangeRateChangeListener(this);

        veriNotificationManager = bitcoinSPV.getVeriNotificationManager();
        veriNotificationManager.clearTransactions();

        mRecyclerView = findViewById(com.memr.bitcoinspv.R.id.recyclerView);
        emptyTextView = findViewById(com.memr.bitcoinspv.R.id.emptyTextView);
        emptyTextView.setText(com.memr.bitcoinspv.R.string.no_transactions);
        mRecyclerView.setEmptyView(emptyTextView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(mDividerItemDecoration);

        // specify an adapter (see also next example)
        if (mAdapter == null) {
            mAdapter = new TransactionListAdapter(kit, TransactionListActivity.this, exchangeManager.getExchangeRate());
            mRecyclerView.setAdapter(mAdapter);
        }

        if (transactionListUpdater == null) {
            transactionListUpdater = new TransactionListUpdater(kit.wallet(), mAdapter);
        }

        transactionListUpdater.updateTransactionList();
        transactionListUpdater.listenForTransactions();

        kit.wallet().addCoinsReceivedEventListener(WalletManager.runInUIThread, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        transactionListUpdater.stopListening();
        kit.wallet().removeCoinsReceivedEventListener(this);
    }

    @Override
    protected void onWalletKitStop() {
        transactionListUpdater.stopListening();
        exchangeManager.removeExchangeRateChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.memr.bitcoinspv.R.menu.transaction_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case com.memr.bitcoinspv.R.id.swapCurrency:
                //Write your code
                mAdapter.swapCurrency();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void exchangeRateUpdated(ExchangeRate exchangeRate) {
        mAdapter.setExchangeRate(exchangeRate);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        veriNotificationManager.clearTransactions();
    }
}
