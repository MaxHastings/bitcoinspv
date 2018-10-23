package com.memr.bitcoinspv;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;

import com.memr.bitcoinspv.Adapters.ContactListAdapterDrag;
import com.memr.bitcoinspv.Managers.ContactManager;
import com.memr.bitcoinspv.Models.Contact;
import com.memr.bitcoinspv.Models.VeriTransaction;
import com.memr.bitcoinspv.Util.RecyclerViewEmptySupport;
import com.memr.bitcoinspv.Util.SendHelper;

public class ContactListActivity extends WalletAppKitActivity implements ContactListAdapterDrag.OnContactListener {

    public final static int REQUEST_CODE = 2;

    private FloatingActionButton addFab;
    private RecyclerViewEmptySupport recyclerView;
    private ContactListAdapterDrag adapter;
    private LinearLayoutManager layoutManager;
    private TextView emptyView;

    private BitcoinSPV bitcoinSPV;

    private ContactManager contactManager;

    public static Intent getIntent(Context context){
        return new Intent(context, ContactListActivity.class);
    }

    @Override
    protected void onWalletKitReady() {
        setContentView(com.memr.bitcoinspv.R.layout.recycler_view_add_button);
        bitcoinSPV = ((BitcoinSPV) getApplication());
        contactManager = bitcoinSPV.getContactManager();

        emptyView = findViewById(com.memr.bitcoinspv.R.id.emptyTextView);
        emptyView.setText(com.memr.bitcoinspv.R.string.no_contacts);
        addFab = findViewById(com.memr.bitcoinspv.R.id.addFab);
        recyclerView = findViewById(com.memr.bitcoinspv.R.id.recyclerView);
        recyclerView.setEmptyView(emptyView);

        if(adapter == null){
            adapter = new ContactListAdapterDrag(this, contactManager, false);
        }

        layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);

        //Allow user to move contacts using dragging gesture
        DragItemTouchHelperCallback dragItemTouchHelperCallback = new DragItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(dragItemTouchHelperCallback);
        touchHelper.attachToRecyclerView(recyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);

        adapter.setContactList(contactManager.getContactList());

        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(AddContactActivity.createIntent(ContactListActivity.this), REQUEST_CODE);
            }
        });

    }

    @Override
    protected void onWalletKitStop() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            adapter.setContactList(contactManager.getContactList()); //Update list if new contact was added.
        }
    }

    public void editContact(Contact contact, int index) {
        startActivityForResult(EditContactActivity.createIntent(this, index, contact), ContactListActivity.REQUEST_CODE);
    }

    public void sendToContact(Contact contact) {
        VeriTransaction veriTransaction = new VeriTransaction();
        veriTransaction.setContact(contact);
        SendHelper sendHelper = new SendHelper(kit, this, veriTransaction);
        sendHelper.startNextActivity();
    }

    @Override
    public void onClick(final Contact contact, final int adapterIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setItems(com.memr.bitcoinspv.R.array.contact_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int choiceIndex) {
                if(choiceIndex == 0){ //Edit
                    editContact(contact, adapterIndex);
                }else{ //Send
                    sendToContact(contact);
                }
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
