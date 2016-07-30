package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.model.Query;
import com.sam_chordas.android.stockhawk.model.Quote;
import com.sam_chordas.android.stockhawk.model.Results;
import com.sam_chordas.android.stockhawk.model.Stock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    public static boolean showPercent = true;
    private static String LOG_TAG = Utils.class.getSimpleName();

    public static ArrayList quoteJsonToContentVals(String JSON, final Stock stock) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                final Query query = stock.getQuery();

                final Quote[] quotes = query.getResults().getQuote();

                for (Quote quote: quotes) {

                    batchOperations.add(buildBatchOperation(quote));

                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(final Quote quote) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);

        String change = quote.Change;
        builder.withValue(QuoteColumns.SYMBOL, quote.symbol);
        builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(quote.Bid));
        builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                quote.ChangeinPercent, true));
        builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
        builder.withValue(QuoteColumns.ISCURRENT, 1);
        if (change.charAt(0) == '-') {
            builder.withValue(QuoteColumns.ISUP, 0);
        } else {
            builder.withValue(QuoteColumns.ISUP, 1);
        }

        return builder.build();
    }
}
