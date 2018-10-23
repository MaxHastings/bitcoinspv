package com.memr.bitcoinspv;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.memr.bitcoinspv.Util.UtilMethods;

import org.bitcoinj.core.Address;

public class ReceiveActivity extends WalletAppKitActivity {

    private TextView receiveView;

    private ImageView qrImageView;

    private ShareActionProvider mShareActionProvider;

    private String receiveAddrString;

    public static Intent createIntent(Context context) {
        return new Intent(context, ReceiveActivity.class);
    }

    @Override
    protected void onWalletKitStop() {

    }

    @Override
    protected void onWalletKitReady() {
        setContentView(com.memr.bitcoinspv.R.layout.activity_receive);

        receiveView = findViewById(com.memr.bitcoinspv.R.id.receiveAddr);
        qrImageView = findViewById(com.memr.bitcoinspv.R.id.qrImage);

        Address receiveAddr = kit.wallet().currentReceiveAddress();
        receiveAddrString = receiveAddr.toString();
        receiveView.setText(receiveAddrString);


        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(receiveAddrString, BarcodeFormat.QR_CODE, 400, 400);
            Bitmap bitmap = UtilMethods.createBitmap(bitMatrix);
            qrImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.memr.bitcoinspv.R.menu.receive_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case com.memr.bitcoinspv.R.id.item_share:
                shareAddress();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareAddress() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, receiveAddrString);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, getResources().getText(com.memr.bitcoinspv.R.string.current_addr_share)));
    }

}
