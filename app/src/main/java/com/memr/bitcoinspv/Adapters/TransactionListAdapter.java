package com.memr.bitcoinspv.Adapters;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.utils.ExchangeRate;
import org.bitcoinj.utils.Fiat;

import java.util.ArrayList;
import java.util.List;

import com.memr.bitcoinspv.R;
import com.memr.bitcoinspv.TransactionDetailActivity;
import com.memr.bitcoinspv.Util.UtilMethods;

public class TransactionListAdapter extends RecyclerView.Adapter<TransactionListAdapter.ViewHolder> {
    private List<Transaction> mDataset = new ArrayList<>();
    private Context context;

    private WalletAppKit kit;
    private ExchangeRate exchangeRate;

    private boolean showFiatAmount = false;

    public void setExchangeRate(ExchangeRate exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public void swapCurrency(){
        showFiatAmount = !showFiatAmount;
        notifyDataSetChanged();
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TransactionListAdapter(WalletAppKit kit, Context context, ExchangeRate exchangeRate) {
        this.kit = kit;
        this.context = context;
        this.exchangeRate = exchangeRate;
    }

    public void setmDataset(List<Transaction> mDataset) {
        this.mDataset = mDataset;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TransactionListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        // create a new view
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        final Transaction tx = mDataset.get(position);

        TransactionConfidence.ConfidenceType confidenceType = tx.getConfidence().getConfidenceType();
        holder.confidenceImage.setImageResource(UtilMethods.getConfidenceResource(confidenceType));

        holder.txHash.setText(tx.getHashAsString());

        holder.date.setText(UtilMethods.getDateString(tx.getUpdateTime()));

        Coin amount = tx.getValue(kit.wallet());
        if (amount.isPositive()) {
            holder.value.setTextColor(context.getResources().getColor(R.color.greenNumber));
        } else {
            holder.value.setTextColor(context.getResources().getColor(android.R.color.primary_text_light));
        }

        if(showFiatAmount){
            Fiat fiat = UtilMethods.roundFiat(exchangeRate.coinToFiat(amount));
            holder.value.setText(fiat.toFriendlyString());
        }else {
            holder.value.setText(amount.toFriendlyString());
        }

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(TransactionDetailActivity.createIntent(context, tx.getHashAsString()));
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ConstraintLayout constraintLayout;
        public TextView txHash;
        public TextView date;
        public TextView value;
        public ImageView confidenceImage;

        public ViewHolder(ConstraintLayout v) {
            super(v);
            constraintLayout = v;
            txHash = v.findViewById(R.id.txHash);
            date = v.findViewById(R.id.date);
            value = v.findViewById(R.id.value);
            confidenceImage = v.findViewById(R.id.confidenceImage);
        }
    }
}
