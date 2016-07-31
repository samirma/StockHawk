package com.sam_chordas.android.stockhawk.presenter;

/**
 * Created by samir on 7/30/16.
 */

public class StockPresenterFactory {


    private static final StockPresenterImpl stockPresenter = new StockPresenterImpl();

    public static StockPresenter getStockPresenter(final StockPresenterView stockPresenterView) {
        stockPresenter.setView(stockPresenterView);
        return stockPresenter;
    }
}
