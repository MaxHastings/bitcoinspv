package com.memr.bitcoinspv;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.memr.bitcoinspv.Adapters.TransactionListAdapter;
import com.memr.bitcoinspv.Managers.ExchangeManager;
import com.memr.bitcoinspv.Managers.VeriNotificationManager;
import com.memr.bitcoinspv.Managers.WalletManager;
import com.memr.bitcoinspv.Util.RecyclerViewEmptySupport;
import com.memr.bitcoinspv.ViewModules.Updaters.BlockchainUpdater;
import com.memr.bitcoinspv.ViewModules.Updaters.PeerGroupUpdater;
import com.memr.bitcoinspv.ViewModules.Updaters.TransactionListUpdater;
import com.memr.bitcoinspv.ViewModules.Updaters.WalletValueUpdater;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.utils.ExchangeRate;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends WalletAppKitActivity implements ExchangeManager.OnExchangeRateChange, WalletCoinsReceivedEventListener{

    private final static int RECENT_TRANSACTION_SIZE = 5;

    private TextView unconfirmedBalance;
    private TextView availableBalance;
    private TextView blockHeight;
    private TextView connectedPeers;
    private TextView emptyTextViewTXs;
    private CardView blockChainView;
    private Button sendButton;
    private Button receiveButton;
    private ConstraintLayout coinBalanceLayout;

    private TextView percentComplete;

    private Button viewTransactionsButton;

    private ConstraintLayout syncingBlock;

    private TextView lastSeenBlockDate;

    private RecyclerViewEmptySupport mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private TransactionListAdapter mAdapter;

    private TransactionListUpdater transactionListUpdater;
    private WalletValueUpdater walletValueUpdater;
    private BlockchainUpdater blockchainUpdater;
    private PeerGroupUpdater peerGroupUpdater;

    private WalletManager walletManager;
    private ExchangeManager exchangeManager;
    private VeriNotificationManager veriNotificationManager;

    private ConstraintLayout fullBalanceLayout;

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onWalletKitReady() {
        setContentView(com.memr.bitcoinspv.R.layout.activity_main);
        walletManager = ((BitcoinSPV) getApplication()).getWalletManager();
        exchangeManager = ((BitcoinSPV) getApplication()).getExchangeManager();
        veriNotificationManager = ((BitcoinSPV) getApplication()).getVeriNotificationManager();
        veriNotificationManager.clearTransactions();

        unconfirmedBalance = findViewById(com.memr.bitcoinspv.R.id.unconfirmedBalance);
        availableBalance = findViewById(com.memr.bitcoinspv.R.id.availableBalance);
        blockHeight = findViewById(com.memr.bitcoinspv.R.id.blockHeight);
        sendButton = findViewById(com.memr.bitcoinspv.R.id.sendButton);
        receiveButton = findViewById(com.memr.bitcoinspv.R.id.receiveButton);
        viewTransactionsButton = findViewById(com.memr.bitcoinspv.R.id.viewTransactionsButton);
        syncingBlock = findViewById(com.memr.bitcoinspv.R.id.synchingBlock);
        connectedPeers = findViewById(com.memr.bitcoinspv.R.id.connectedPeers);
        percentComplete = findViewById(com.memr.bitcoinspv.R.id.percentComplete);
        lastSeenBlockDate = findViewById(com.memr.bitcoinspv.R.id.lastSeenBlockDate);
        mRecyclerView = findViewById(com.memr.bitcoinspv.R.id.recyclerView);
        blockChainView = findViewById(com.memr.bitcoinspv.R.id.blockChainCard);
        emptyTextViewTXs = findViewById(com.memr.bitcoinspv.R.id.emptyTextViewTXs);
        coinBalanceLayout = findViewById(com.memr.bitcoinspv.R.id.coinBalanceLayout);

        fullBalanceLayout = findViewById(com.memr.bitcoinspv.R.id.fullBalanceLayout);

        mRecyclerView.setEmptyView(emptyTextViewTXs);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setEmptyViewListener(new RecyclerViewEmptySupport.OnEmptyViewListener() {
            @Override
            public void emptyViewIsOn() {
                viewTransactionsButton.setVisibility(View.GONE);
            }

            @Override
            public void emptyViewIsOff() {
                viewTransactionsButton.setVisibility(View.VISIBLE);
            }
        });

        blockChainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(PeerGroupListActivity.createIntent(MainActivity.this));
            }
        });

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(mDividerItemDecoration);

        viewTransactionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(TransactionListActivity.createIntent(MainActivity.this));
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(RecipientActivity.createIntent(MainActivity.this));
            }
        });

        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ReceiveActivity.createIntent(MainActivity.this));
            }
        });

        coinBalanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Swap between Fiat and Coin
                walletValueUpdater.setExchangeRate(exchangeManager.getExchangeRate());
                walletValueUpdater.swapCurrency();
                mAdapter.swapCurrency();
            }
        });

        // specify an adapter (see also next example)
        if (mAdapter == null) {
            mAdapter = new TransactionListAdapter(kit, MainActivity.this, exchangeManager.getExchangeRate());
            mRecyclerView.setAdapter(mAdapter);
        }

        if (transactionListUpdater == null) {
            transactionListUpdater = new TransactionListUpdater(kit.wallet(), mAdapter, RECENT_TRANSACTION_SIZE);
        }

        transactionListUpdater.updateTransactionList();
        transactionListUpdater.listenForTransactions();

        if (walletValueUpdater == null) {
            walletValueUpdater = new WalletValueUpdater(kit.wallet(), availableBalance, unconfirmedBalance);
        }

        walletValueUpdater.updateWalletView();
        walletValueUpdater.listenForBalanceChanges();

        if (blockchainUpdater == null) {
            blockchainUpdater = new BlockchainUpdater(walletManager, kit.chain(), syncingBlock, percentComplete, blockHeight, lastSeenBlockDate);
        }

        blockchainUpdater.updateBlockChainView();
        blockchainUpdater.listenForBlocks();

        if (peerGroupUpdater == null) {
            peerGroupUpdater = new PeerGroupUpdater(kit.peerGroup(), connectedPeers);
        }

        peerGroupUpdater.updatePeerView();
        peerGroupUpdater.listenForPeerConnections();

        exchangeManager.addExchangeRateChangeListener(this);
        kit.wallet().addCoinsReceivedEventListener(WalletManager.runInUIThread, this);
    }

    @Override
    protected void onWalletKitStop() {
        stopListeners();
    }

    public void stopListeners() {
        transactionListUpdater.stopListening();
        walletValueUpdater.stopListening();
        blockchainUpdater.stopListening();
        peerGroupUpdater.stopListening();
        exchangeManager.removeExchangeRateChangeListener(this);
        kit.wallet().removeCoinsReceivedEventListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.memr.bitcoinspv.R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case com.memr.bitcoinspv.R.id.settings:
                //Write your code
                startActivity(SettingsActivity.createIntent(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void exchangeRateUpdated(ExchangeRate exchangeRate) {
        walletValueUpdater.setExchangeRate(exchangeRate);
        walletValueUpdater.updateWalletView();

        mAdapter.setExchangeRate(exchangeRate);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        veriNotificationManager.clearTransactions();
    }
}
