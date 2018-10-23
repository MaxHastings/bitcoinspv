package com.memr.bitcoinspv;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.memr.bitcoinspv.Adapters.InputListAdapter;
import com.memr.bitcoinspv.Adapters.OutputListAdapter;
import com.memr.bitcoinspv.Managers.ExchangeManager;
import com.memr.bitcoinspv.Util.UtilMethods;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.utils.ExchangeRate;

public class TransactionDetailActivity extends WalletAppKitActivity implements ExchangeManager.OnExchangeRateChange {

    private final static String TX_EXTRA = "Transaction";

    private Transaction tx;

    private String txString;

    private RecyclerView inputRecyclerView;
    private InputListAdapter inputAdapter;

    private RecyclerView outputRecyclerView;
    private OutputListAdapter outputAdapter;

    private TextView amountView;
    private TextView txHashView;
    private TextView confirmationView;
    private TextView feeView;
    private TextView dateView;
    private TextView confidenceTypeView;
    private ImageView confidenceImageView;

    private LinearLayoutManager inputLayoutManager;
    private LinearLayoutManager outputLayoutManager;

    private BitcoinSPV bitcoinSPV;
    private ExchangeManager exchangeManager;

    private boolean showFiatCurrency = false;

    public static Intent createIntent(Context context, String txString) {
        Intent intent = new Intent(context, TransactionDetailActivity.class);
        intent.putExtra(TX_EXTRA, txString);
        return intent;
    }

    @Override
    protected void onWalletKitStop() {
        exchangeManager.removeExchangeRateChangeListener(this);
    }

    @Override
    protected void onWalletKitReady() {
        setContentView(com.memr.bitcoinspv.R.layout.activity_transaction_details);
        bitcoinSPV = (BitcoinSPV) getApplication();
        exchangeManager = bitcoinSPV.getExchangeManager();
        exchangeManager.addExchangeRateChangeListener(this);

        amountView = findViewById(com.memr.bitcoinspv.R.id.amount);
        txHashView = findViewById(com.memr.bitcoinspv.R.id.txHash);
        feeView = findViewById(com.memr.bitcoinspv.R.id.fee);
        confirmationView = findViewById(com.memr.bitcoinspv.R.id.confirmations);
        dateView = findViewById(com.memr.bitcoinspv.R.id.date);
        confidenceTypeView = findViewById(com.memr.bitcoinspv.R.id.confidenceType);
        confidenceImageView = findViewById(com.memr.bitcoinspv.R.id.confidenceImage);

        inputLayoutManager = new LinearLayoutManager(this);
        outputLayoutManager = new LinearLayoutManager(this);
        inputRecyclerView = findViewById(com.memr.bitcoinspv.R.id.inputRecyclerView);
        outputRecyclerView = findViewById(com.memr.bitcoinspv.R.id.outputRecyclerView);
        inputRecyclerView.setNestedScrollingEnabled(false);
        outputRecyclerView.setNestedScrollingEnabled(false);

        inputRecyclerView.setLayoutManager(inputLayoutManager);
        outputRecyclerView.setLayoutManager(outputLayoutManager);

        DividerItemDecoration inputDivider = new DividerItemDecoration(inputRecyclerView.getContext(),
                inputLayoutManager.getOrientation());
        inputRecyclerView.addItemDecoration(inputDivider);

        DividerItemDecoration outputDivider = new DividerItemDecoration(outputRecyclerView.getContext(),
                outputLayoutManager.getOrientation());
        outputRecyclerView.addItemDecoration(outputDivider);

        txString = getIntent().getStringExtra(TX_EXTRA);

        tx = kit.wallet().getTransaction(Sha256Hash.wrap(txString));

        inputAdapter = new InputListAdapter(kit, tx.getInputs(), exchangeManager.getExchangeRate());
        outputAdapter = new OutputListAdapter(kit, tx.getOutputs(), exchangeManager.getExchangeRate());
        inputRecyclerView.setAdapter(inputAdapter);
        outputRecyclerView.setAdapter(outputAdapter);

        setUpTransactionDetails();
    }

    public void setUpTransactionDetails() {

        Coin amount = tx.getValue(kit.wallet());
        if (amount.isPositive()) {
            amountView.setTextColor(getResources().getColor(com.memr.bitcoinspv.R.color.greenNumber));
        }

        if(showFiatCurrency){
            amountView.setText(UtilMethods.roundFiat(exchangeManager.getExchangeRate().coinToFiat(amount)).toFriendlyString());
        }else {
            amountView.setText(amount.toFriendlyString());
        }

        Coin fee = tx.getFee();
        if (fee == null) {
            feeView.setText(com.memr.bitcoinspv.R.string.N_A); //If transaction wasn't sent by us we don't know fee.
        } else {
            if(showFiatCurrency){
                feeView.setText(UtilMethods.roundFiat(exchangeManager.getExchangeRate().coinToFiat(fee)).toFriendlyString());
            }else {
                feeView.setText(fee.toFriendlyString());
            }
        }
        txHashView.setText(tx.getHashAsString());

        dateView.setText(UtilMethods.getDateTimeString(tx.getUpdateTime()));
        confidenceTypeView.setText(UtilMethods.getConfidenceString(tx.getConfidence().getConfidenceType()));
        confidenceImageView.setImageResource(UtilMethods.getConfidenceResource(tx.getConfidence().getConfidenceType()));
        confirmationView.setText(String.valueOf(tx.getConfidence().getDepthInBlocks()));
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
                showFiatCurrency = !showFiatCurrency;
                setUpTransactionDetails();

                inputAdapter.swapCurrency();
                outputAdapter.swapCurrency();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void exchangeRateUpdated(ExchangeRate exchangeRate) {
        setUpTransactionDetails();
        inputAdapter.setExchangeRate(exchangeRate);
        outputAdapter.setExchangeRate(exchangeRate);
        inputAdapter.notifyDataSetChanged();
        outputAdapter.notifyDataSetChanged();
    }
}
