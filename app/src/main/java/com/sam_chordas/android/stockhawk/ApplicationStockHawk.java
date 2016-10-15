package com.sam_chordas.android.stockhawk;

import android.app.Application;
import android.content.Context;

/**
 * Created by samir on 7/28/16.
 */
public class ApplicationStockHawk extends Application {
    private static Context context = null;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getBaseContext();


    }
    public static Context getContext() {
        return context;
    }
}
