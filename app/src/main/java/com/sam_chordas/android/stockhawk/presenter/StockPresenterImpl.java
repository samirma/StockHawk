package com.sam_chordas.android.stockhawk.presenter;

import android.content.Context;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.util.NetworkUtil;

/**
 * Created by samir on 7/30/16.
 */

public class StockPresenterImpl implements StockPresenter {
    private StockPresenterView mView;
    private Context mContext;

    public void setView(final StockPresenterView view) {
        this.mView = view;
        this.mContext = view.getContext();
    }

    @Override
    public void startPresenter() {
        if (NetworkUtil.isConnect()) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(mContext).schedule(periodicTask);
        }
    }

}
