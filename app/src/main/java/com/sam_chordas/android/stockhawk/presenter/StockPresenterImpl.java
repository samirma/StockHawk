package com.sam_chordas.android.stockhawk.presenter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;
import com.sam_chordas.android.stockhawk.ui.fragments.StockDetailFragment;
import com.sam_chordas.android.stockhawk.util.NetworkUtil;

/**
 * Created by samir on 7/30/16.
 */

public class StockPresenterImpl implements StockPresenter, AddStockCallBack {
    private StockPresenterView mView;
    private Context mContext;
    private Intent mServiceIntent;

    public void setView(final StockPresenterView view) {
        this.mView = view;
        this.mContext = view.getContext();
    }

    @Override
    public void startPresenter() {

        mServiceIntent = new Intent(mContext, StockIntentService.class);


        if (NetworkUtil.isConnect()) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(mContext).schedule(periodicTask);
        }
    }

    @Override
    public void addStockRequired() {
        final boolean isConnected = NetworkUtil.isConnect();
        if (isConnected) {
            mView.enableAddStock();
        } else {
            mView.networkToast();
        }
        mView.showMessage(isConnected);
    }

    @Override
    public void startInitService() {
        mServiceIntent.putExtra(StockTaskService.TAG, StockTaskService.INIT);
        final boolean isConnected = NetworkUtil.isConnect();
        if (isConnected) {
            mContext.startService(mServiceIntent);
        } else {
            mView.networkToast();
        }

        mView.showMessage(isConnected);
    }

    @Override
    public void addStock(CharSequence input) {
        mServiceIntent.putExtra(StockTaskService.TAG, StockTaskService.ADD);
        mServiceIntent.putExtra(StockTaskService.SYMBOL, input.toString());
        mContext.startService(mServiceIntent);
    }

    @Override
    public void selectStock(Cursor cursor) {
        final Intent intent = new Intent(mContext, StockDetailActivity.class);
        intent.putExtra(StockDetailFragment.STOCK_ID, cursor.getString(cursor.getColumnIndex(QuoteCursorAdapter.SYMBOL)));
        mContext.startActivity(intent);
    }

    @Override
    public void setError(Exception e) {
        mView.showAddStockFail(mContext.getString(R.string.fail_add));
    }
}
