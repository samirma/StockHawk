package com.sam_chordas.android.stockhawk.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.fragments.StockDetailFragment;

public class StockDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        // Create a new Fragment to be placed in the activity layout
        StockDetailFragment firstFragment = new StockDetailFragment();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        final Bundle extras = getIntent().getExtras();
        final String stock = getIntent().getExtras().getString(StockDetailFragment.STOCK_ID);
        extras.putString(StockDetailFragment.STOCK_ID, stock);
        firstFragment.setArguments(extras);

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment, firstFragment).commit();

    }
}
