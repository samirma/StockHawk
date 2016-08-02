package com.sam_chordas.android.stockhawk.presenter;

import android.content.Context;

/**
 * Created by samir on 7/30/16.
 */

public interface StockPresenterView {
    Context getContext();

    void showMessage(boolean showMessage);

    void enableAddStock();

    void networkToast();
}
