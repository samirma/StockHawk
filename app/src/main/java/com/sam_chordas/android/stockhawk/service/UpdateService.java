package com.sam_chordas.android.stockhawk.service;

import android.content.SharedPreferences;

import com.sam_chordas.android.stockhawk.AplicationStockHawk;

import java.util.Date;

/**
 * Created by samir on 8/5/16.
 */
public class UpdateService {

    public static final String UPDATE_SERVICE = "UPDATE_SERVICE";
    public static final SharedPreferences SHARED_PREFERENCES = AplicationStockHawk.getContext().getSharedPreferences(UPDATE_SERVICE, 0);
    public static final String LAST_UPDATE_KEY = "LAST_UPDATE_KEY";
    public static final int DATED_TIMEOUT = 1000 * 60 * 60;

    public void save() {
        SharedPreferences settings = SHARED_PREFERENCES;
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(LAST_UPDATE_KEY, getTime());
        // Commit the edits!
        editor.commit();
    }

    public long getTime() {
        return new Date().getTime();
    }

    public boolean isDated(){
        SharedPreferences settings = SHARED_PREFERENCES;
        final long aLong = settings.getLong(LAST_UPDATE_KEY, 0);

        final boolean isDated = (getTime() - aLong) < DATED_TIMEOUT;
        return isDated;
    }

}
