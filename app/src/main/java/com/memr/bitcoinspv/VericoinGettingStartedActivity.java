package com.memr.bitcoinspv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class VericoinGettingStartedActivity extends AppCompatActivity {

    private Button nextButton;

    public static Intent createIntent(Context context) {
        return new Intent(context, VericoinGettingStartedActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.memr.bitcoinspv.R.layout.activity_vericoin_getting_started);

        nextButton = findViewById(com.memr.bitcoinspv.R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(VeriumGettingStartedActivity.createIntent(VericoinGettingStartedActivity.this));
            }
        });

    }
}
