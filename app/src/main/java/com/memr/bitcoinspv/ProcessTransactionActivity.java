package com.memr.bitcoinspv;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.memr.bitcoinspv.Managers.WalletManager;
import com.memr.bitcoinspv.Models.VeriTransaction;

import org.bitcoinj.core.Address;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.View.GONE;

public class ProcessTransactionActivity extends WalletAppKitActivity {

    private final static String VERI_TRANSACTION = "veriTransaction";

    private TextView txHashView;

    private ConstraintLayout txHashBox;

    private TextView statusView;

    private ImageView completeImage;

    private Button doneButton;

    private ProgressBar progressBar;

    private VeriTransaction veriTransaction;

    public static Intent createIntent(Context context, VeriTransaction veriTransaction) {
        Intent intent = new Intent(context, ProcessTransactionActivity.class);
        intent.putExtra(VERI_TRANSACTION, veriTransaction);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onWalletKitStop() {

    }

    @Override
    protected void onWalletKitReady() {
        setContentView(com.memr.bitcoinspv.R.layout.activity_process_transaction);

        veriTransaction = (VeriTransaction) getIntent().getSerializableExtra(VERI_TRANSACTION);

        txHashView = findViewById(com.memr.bitcoinspv.R.id.txHash);
        txHashBox = findViewById(com.memr.bitcoinspv.R.id.txHashBox);
        statusView = findViewById(com.memr.bitcoinspv.R.id.statusView);
        completeImage = findViewById(com.memr.bitcoinspv.R.id.completeImage);
        progressBar = findViewById(com.memr.bitcoinspv.R.id.progressBar);

        doneButton = findViewById(com.memr.bitcoinspv.R.id.doneButton);

        doneButton.setVisibility(GONE);
        completeImage.setVisibility(GONE);
        txHashBox.setVisibility(GONE);

        statusView.setText(getString(com.memr.bitcoinspv.R.string.creating_transaction));

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.createIntent(ProcessTransactionActivity.this));
                finish();
            }
        });

        sendTransaction();
    }

    public void sendTransaction() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    String addressString = veriTransaction.getContact().getAddress();
                    SendRequest request = SendRequest.to(Address.fromString(kit.params(), addressString), veriTransaction.getAmount());
                    request.staticFee = veriTransaction.getFee();

                    if (kit.wallet().isEncrypted()) { //If password is required to decrypt wallet add it to request.
                        request.aesKey = kit.wallet().getKeyCrypter().deriveKey(veriTransaction.getPassword());
                    }

                    final Wallet.SendResult sendResult = kit.wallet().sendCoins(request);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            broadcastWaiting(); //Transaction sent. Wait for broadcast to complete.
                        }
                    });

                    // Register a callback that is invoked when the transaction has propagated across the network.
                    // This shows a second style of registering ListenableFuture callbacks, it works when you don't
                    // need access to the object the future returns.
                    sendResult.broadcastComplete.addListener(new Runnable() {
                        @Override
                        public void run() {
                            broadcastComplete(sendResult.tx.getHashAsString()); //Broadcast complete show user TX hash.
                        }
                    }, WalletManager.runInUIThread);
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            broadcastFailed(e.toString());
                        }
                    });
                }
            }
        }).start();
    }

    public void broadcastWaiting() {
        statusView.setText(getString(com.memr.bitcoinspv.R.string.broadcasting_transaction));
    }

    public void broadcastComplete(String txHash) {
        txHashView.setText(txHash);
        progressBar.setVisibility(GONE);
        doneButton.setVisibility(View.VISIBLE);
        completeImage.setVisibility(View.VISIBLE);
        txHashBox.setVisibility(View.VISIBLE);
        statusView.setText(getString(com.memr.bitcoinspv.R.string.broadcast_complete));
    }

    public void broadcastFailed(String message) {
        Toast.makeText(ProcessTransactionActivity.this, message, Toast.LENGTH_LONG).show();
        progressBar.setVisibility(GONE);
        doneButton.setVisibility(View.VISIBLE);
        statusView.setText(getString(com.memr.bitcoinspv.R.string.broadcast_failed));
    }

    @Override
    public void onBackPressed() {
        //Do nothing. (We don't want user to go back while a transaction is being processed.)
    }

}
