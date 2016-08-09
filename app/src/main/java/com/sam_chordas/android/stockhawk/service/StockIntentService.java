package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.presenter.StockPresenterFactory;

import static com.sam_chordas.android.stockhawk.data.QuoteColumns.SYMBOL;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

    public static final String TAG = StockIntentService.class.getSimpleName();

    public StockIntentService() {
        super(StockIntentService.class.getName());
    }

    public StockIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(StockIntentService.class.getSimpleName(), "Stock Intent Service");
        StockTaskService stockTaskService = new StockTaskService(this);
        Bundle args = new Bundle();
        final String tag = intent.getStringExtra(StockTaskService.TAG);
        if (tag.equals(StockTaskService.ADD)) {
            args.putString(SYMBOL, intent.getStringExtra(SYMBOL));
        }
        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        try {
            stockTaskService.onRunTask(new TaskParams(tag, args));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            StockPresenterFactory.getAddStockCallBack().setError(e);
        }
    }
}
