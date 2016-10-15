package com.sam_chordas.android.stockhawk.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.sam_chordas.android.stockhawk.ApplicationStockHawk;

/**
 * Created by samir on 7/28/16.
 */

public class NetworkUtil {

    public static final ConnectivityManager SYSTEM_SERVICE = (ConnectivityManager) ApplicationStockHawk.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

    public static boolean isConnect() {
        ConnectivityManager cm =
                SYSTEM_SERVICE;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        final boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}
