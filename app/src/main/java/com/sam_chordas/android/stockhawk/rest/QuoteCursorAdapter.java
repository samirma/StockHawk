package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.presenter.StockPresenter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sam_chordas on 10/6/15.
 * Credit to skyfishjy gist:
 * https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    public static final String SYMBOL = "symbol";
    public static final String BID_PRICE = "bid_price";
    public static final String PERCENT_CHANGE = "percent_change";
    public static final String CHANGE = "change";
    private Context mContext;
    private static Typeface robotoLight;
    private final StockPresenter mPresenter;

    public QuoteCursorAdapter(Context context, Cursor cursor, StockPresenter presenter) {
        super(context, cursor);
        mContext = context;
        mPresenter = presenter;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_quote, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
        final String stockName = cursor.getString(cursor.getColumnIndex(SYMBOL));
        viewHolder.symbol.setText(stockName);
        viewHolder.bidPrice.setText(cursor.getString(cursor.getColumnIndex(BID_PRICE)));

        final int color;

        if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1) {
            color = R.drawable.percent_change_pill_green;
        } else {
            color = R.drawable.percent_change_pill_red;
        }
        setBackground(viewHolder, color);


        final String text;

        if (ResultUtil.showPercent) {
           text = cursor.getString(cursor.getColumnIndex(PERCENT_CHANGE));
        } else {
            text = cursor.getString(cursor.getColumnIndex(CHANGE));
        }
        viewHolder.change.setText(text);

        viewHolder.itemView.setContentDescription(String.format("select %s", stockName));

    }

    private void setBackground(ViewHolder viewHolder, int percent_change_pill_red) {
        int sdk = Build.VERSION.SDK_INT;

        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            viewHolder.change.setBackgroundDrawable(
                    mContext.getResources().getDrawable(percent_change_pill_red));
        } else {
            viewHolder.change.setBackground(
                    mContext.getResources().getDrawable(percent_change_pill_red));
        }
    }

    @Override
    public void onItemDismiss(int position) {
        Cursor c = getCursor();
        c.moveToPosition(position);
        String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
        mContext.getContentResolver().delete(QuoteProvider.Quotes.withSymbol(symbol), null, null);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        final int itemCount = super.getItemCount();
        return itemCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements ItemTouchHelperViewHolder, View.OnClickListener {
        @BindView(R.id.stock_symbol)
        public TextView symbol;

        @BindView(R.id.bid_price)
        public TextView bidPrice;

        @BindView(R.id.change)
        public TextView change;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            symbol.setTypeface(robotoLight);

        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
