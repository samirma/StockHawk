package com.sam_chordas.android.stockhawk.presenter;

import android.database.Cursor;
import android.view.View;

/**
 * Created by samir on 7/30/16.
 */

public interface StockPresenter {
    void startPresenter();

    void addStockRequired();

    void startInitService();

    void addStock(CharSequence input);

    void selectStock(Cursor cursor, View v);

    void reloadStocks();
}
