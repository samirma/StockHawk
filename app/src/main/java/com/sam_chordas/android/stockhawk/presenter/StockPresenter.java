package com.sam_chordas.android.stockhawk.presenter;

/**
 * Created by samir on 7/30/16.
 */

public interface StockPresenter {
    void startPresenter();

    void addStockRequired();

    void startInitService();

    void addStock(CharSequence input);
}
