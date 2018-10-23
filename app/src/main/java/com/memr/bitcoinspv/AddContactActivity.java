package com.memr.bitcoinspv;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.memr.bitcoinspv.Managers.ContactManager;
import com.memr.bitcoinspv.Managers.WalletManager;
import com.memr.bitcoinspv.Models.Contact;

import org.bitcoinj.core.Address;

public class AddContactActivity extends WalletAppKitActivity {

    private ConstraintLayout scanButton;
    private TextInputLayout contactLayout;
    private TextInputLayout addressLayout;
    private Button addContactButton;

    private ContactManager contactManager;

    private BitcoinSPV bitcoinSPV;

    private WalletManager walletManager;

    public static Intent createIntent(Context context){
        return new Intent(context, AddContactActivity.class);
    }

    @Override
    protected void onWalletKitReady() {
        setContentView(com.memr.bitcoinspv.R.layout.activity_add_contact);

        bitcoinSPV = ((BitcoinSPV) getApplication());
        walletManager = bitcoinSPV.getWalletManager();
        contactManager = bitcoinSPV.getContactManager();

        scanButton = findViewById(com.memr.bitcoinspv.R.id.scanButton);
        contactLayout = findViewById(com.memr.bitcoinspv.R.id.contactLayout);
        addressLayout = findViewById(com.memr.bitcoinspv.R.id.addressLayout);
        addContactButton = findViewById(com.memr.bitcoinspv.R.id.addContactButton);

        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Address.fromString(walletManager.getParams(), getAddress()); //Check to see if Address is valid
                    if(isNameEmpty()){
                        contactLayout.setError(getString(com.memr.bitcoinspv.R.string.invalid_input));
                    }else{
                        addContact(getName(), getAddress());
                        Toast.makeText(AddContactActivity.this, com.memr.bitcoinspv.R.string.contact_added, Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                }catch(Exception e){
                    addressLayout.setError(getString(com.memr.bitcoinspv.R.string.invalid_address));
                }
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(AddContactActivity.this).initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                addressLayout.getEditText().setText(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public String getAddress(){
        return addressLayout.getEditText().getText().toString();
    }

    public String getName(){
        return contactLayout.getEditText().getText().toString();
    }

    public boolean isAddressEmpty(){
        return getAddress().isEmpty();
    }

    public boolean isNameEmpty(){
        return getName().isEmpty();
    }

    public void addContact(String name, String address){
        contactManager.addContact(new Contact(name, address));
    }
}
