package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {
    public static final String PERIODIC = "periodic";
    public static final String INIT = "init";
    public static final String SYMBOL = "symbol";
    public static final String ADD = "add";
    public static final String TAG = "tag";
    private String LOG_TAG = StockTaskService.class.getSimpleName();

    private Context mContext;

    public StockTaskService() {
    }

    public StockTaskService(Context context) {
        mContext = context;
    }



    @Override
    public int onRunTask(TaskParams params) {

        if (mContext == null) {
            mContext = this;
        }

        final String tag = params.getTag();
        final Bundle extras = params.getExtras();
        String stockInput = "";
        if (extras != null){
            stockInput = extras.getString(SYMBOL);
        }


        return new LoadStock(tag, stockInput).invoke();
    }


}
