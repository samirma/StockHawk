package com.sam_chordas.android.stockhawk.service;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.sam_chordas.android.stockhawk.ApplicationStockHawk;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.ResultUtil;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import static com.sam_chordas.android.stockhawk.data.QuoteColumns.SYMBOL;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.INIT;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.PERIODIC;
import static com.sam_chordas.android.stockhawk.widget.StockHawkAppWidget.ACTION_DATA_UPDATED;

public class LoadStock {
    public static final String TAG = LoadStock.class.getSimpleName();
    private Context mContext = ApplicationStockHawk.getContext();
    ;
    private String tag;
    private String stockInput;
    private boolean isUpdate;
    private StringBuilder mStoredSymbols = new StringBuilder();

    private OkHttpClient client = new OkHttpClient();

    public LoadStock(String tag, String stockInput) {
        this.tag = tag;
        this.stockInput = stockInput;
    }

    public LoadStock() {
    }

    public int updateFromServer() {
        Cursor initQueryCursor;
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                    + "in (", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final ContentResolver contentResolver = mContext.getContentResolver();
        switch (tag) {
            case INIT:
            case PERIODIC:
                isUpdate = true;
                initQueryCursor = contentResolver.query(QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{"Distinct " + SYMBOL}, null,
                        null, null);
                if (initQueryCursor.getCount() == 0 || initQueryCursor == null) {
                    // Init task. Populates DB with quotes for the symbols seen below
                    try {
                        urlStringBuilder.append(
                                URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else if (initQueryCursor != null) {
                    DatabaseUtils.dumpCursor(initQueryCursor);
                    initQueryCursor.moveToFirst();
                    for (int i = 0; i < initQueryCursor.getCount(); i++) {
                        mStoredSymbols.append("\"" +
                                initQueryCursor.getString(initQueryCursor.getColumnIndex(SYMBOL)) + "\",");
                        initQueryCursor.moveToNext();
                    }
                    mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
                    try {
                        urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case StockTaskService.ADD:
                isUpdate = false;
                // get symbol from params.getExtra and build query
                try {
                    urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
        }
        // finalize the URL for the API query.
        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

        String urlString;
        String getResponse;
        int result = GcmNetworkManager.RESULT_FAILURE;

        if (urlStringBuilder != null) {
            urlString = urlStringBuilder.toString();
            try {
                getResponse = fetchData(urlString);
                result = GcmNetworkManager.RESULT_SUCCESS;
                try {
                    ContentValues contentValues = new ContentValues();
                    // update ISCURRENT to 0 (false) so new data is current
                    if (isUpdate) {
                        contentValues.put(QuoteColumns.ISCURRENT, 0);
                        contentResolver.update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                                null, null);
                    }
                    contentResolver.applyBatch(QuoteProvider.AUTHORITY,
                            ResultUtil.quoteJsonToContentVals(getResponse));

                    new UpdateService().save(new Date());

                    updateWidgets();

                } catch (RemoteException | OperationApplicationException e) {
                    Log.e(TAG, "Error applying batch insert", e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public Cursor updateFromDatebase() {
        final Cursor query = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.ID, SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
        return query;
    }

    private void updateWidgets() {
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(mContext.getPackageName());
        mContext.sendBroadcast(dataUpdatedIntent);
    }

}