package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.ResultUtil;
import com.sam_chordas.android.stockhawk.service.LoadStock;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;
import com.sam_chordas.android.stockhawk.ui.fragments.StockDetailFragment;

import static com.sam_chordas.android.stockhawk.R.drawable.percent_change_pill_red;
import static com.sam_chordas.android.stockhawk.data.QuoteColumns.BIDPRICE;
import static com.sam_chordas.android.stockhawk.data.QuoteColumns.CHANGE;
import static com.sam_chordas.android.stockhawk.data.QuoteColumns.PERCENT_CHANGE;
import static com.sam_chordas.android.stockhawk.data.QuoteColumns.SYMBOL;

/**
 * RemoteViewsService controlling the cursor being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    private static final int ID = 0;
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private final LoadStock loadStock = new LoadStock();
            private Cursor cursor = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null) {
                    cursor.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                cursor = loadStock.updateFromDatebase();

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            @Override
            public int getCount() {
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        cursor == null || !cursor.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.list_item_quote);

                final String stockName = cursor.getString(cursor.getColumnIndex(SYMBOL));
                views.setTextViewText(R.id.stock_symbol, stockName);
                views.setTextViewText(R.id.bid_price, cursor.getString(cursor.getColumnIndex(BIDPRICE)));

                final String text;

                if (ResultUtil.showPercent) {
                    text = cursor.getString(cursor.getColumnIndex(PERCENT_CHANGE));
                } else {
                    text = cursor.getString(cursor.getColumnIndex(CHANGE));
                }
                views.setTextViewText(R.id.change, text);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, stockName);
                }

                final Intent intent = new Intent(getBaseContext(), StockDetailActivity.class);
                intent.putExtra(StockDetailFragment.STOCK_ID, stockName);
                views.setOnClickFillInIntent(R.id.widget_list_item, intent);

                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_list_item, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (cursor.moveToPosition(position))
                    return cursor.getLong(ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
