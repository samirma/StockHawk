package com.sam_chordas.android.stockhawk.presenter;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by samir on 7/30/16.
 */

public interface StockPresenterView {
    Context getContext();

    void getStockName();

    void networkToast();

    void showAddStockFail(String s);

    void resetData();

    void setData(Cursor data);

    void showStockList();

    void showDatedMessage();

    void hideDatedMessage();

    void hideStockList();

    void hideAddStock();

    void enableAddStock();

    void showNoNetworkMessage();

    void hideNoNetworkMessage();
}
