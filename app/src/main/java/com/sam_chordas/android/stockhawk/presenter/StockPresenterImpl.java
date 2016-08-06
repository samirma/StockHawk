package com.sam_chordas.android.stockhawk.presenter;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.service.UpdateService;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;
import com.sam_chordas.android.stockhawk.ui.fragments.StockDetailFragment;
import com.sam_chordas.android.stockhawk.util.NetworkUtil;

/**
 * Created by samir on 7/30/16.
 */

public class StockPresenterImpl implements StockPresenter, AddStockCallBack, LoaderManager.LoaderCallbacks<Cursor> {
    private StockPresenterView mView;
    private Context mContext;
    private Intent mServiceIntent;
    private LoaderManager loaderManager;
    private static final int CURSOR_LOADER_ID = 0;
    private UpdateService updateService;


    public void setView(final StockPresenterView view) {
        this.mView = view;
        this.mContext = view.getContext();
        updateService = new UpdateService();
    }

    @Override
    public void startPresenter() {

        loaderManager = ((Activity)mContext).getLoaderManager();

        loaderManager.initLoader(CURSOR_LOADER_ID, null, this);


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
            mView.getStockName();
        } else {
            mView.networkToast();
        }
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
    public void reloadStocks() {
        loaderManager.restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public void setError(Exception e) {
        mView.showAddStockFail(mContext.getString(R.string.fail_add));
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        final boolean isNotEmpty = data.getCount() > 0;
        if (isNotEmpty) {
            mView.showStockList();
            final boolean dated = updateService.isDated();
            if (dated) {
                mView.showDatedMessage();
            } else {
                mView.hideDatedMessage();
            }
        } else {
            mView.hideStockList();
            mView.hideDatedMessage();
        }

        if (NetworkUtil.isConnect()) {
            mView.enableAddStock();
            mView.hideNoNetworkMessage();
        } else {
            mView.hideAddStock();
            mView.showNoNetworkMessage();
        }

        mView.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mView.resetData();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(mContext, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }
}
