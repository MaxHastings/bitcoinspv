package com.memr.bitcoinspv;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.memr.bitcoinspv.Adapters.CustomPeerListAdapter;
import com.memr.bitcoinspv.Util.RecyclerViewEmptySupport;

public class CustomPeerListActivity extends WalletAppKitActivity {

    private final static int REQUEST_CODE = 1;

    private RecyclerViewEmptySupport recyclerView;
    private CustomPeerListAdapter adapter;
    private LinearLayoutManager layoutManager;
    private TextView emptyTextView;

    private FloatingActionButton addPeerfab;

    private BitcoinSPV bitcoinSPV;

    public static Intent createIntent(Context context) {
        return new Intent(context, CustomPeerListActivity.class);
    }

    public BitcoinSPV getBitcoinSPV() {
        return bitcoinSPV;
    }

    @Override
    protected void onWalletKitReady() {
        setContentView(com.memr.bitcoinspv.R.layout.recycler_view_add_button);
        bitcoinSPV = (BitcoinSPV) getApplication();

        emptyTextView = findViewById(com.memr.bitcoinspv.R.id.emptyTextView);
        emptyTextView.setText(com.memr.bitcoinspv.R.string.edit_peers_desc);
        recyclerView = findViewById(com.memr.bitcoinspv.R.id.recyclerView);
        recyclerView.setEmptyView(emptyTextView
        );
        addPeerfab = findViewById(com.memr.bitcoinspv.R.id.addFab);

        if (adapter == null) {
            adapter = new CustomPeerListAdapter(this);
        }

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);

        recyclerView.setAdapter(adapter);

        addPeerfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(AddPeerActivity.createIntent(CustomPeerListActivity.this), REQUEST_CODE);
            }
        });

        adapter.setPeerList(bitcoinSPV.getPeerManager().getCustomPeerAddressList());
    }

    @Override
    protected void onWalletKitStop() {
        addPeerfab.setOnClickListener(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            adapter.setPeerList(bitcoinSPV.getPeerManager().getCustomPeerAddressList()); //Update list if new peer was added.
        }
    }
}
