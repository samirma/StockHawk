package com.sam_chordas.android.stockhawk.service;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
    private StringBuilder mStoredSymbols = new StringBuilder();

    public StockTaskService() {
    }

    public StockTaskService(Context context) {
        mContext = context;
    }



    @Override
    public int onRunTask(TaskParams params) {
        Cursor initQueryCursor;
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
