package com.sam_chordas.android.stockhawk.ui.fragments;


import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.presenter.StockPresenter;
import com.sam_chordas.android.stockhawk.presenter.StockPresenterFactory;
import com.sam_chordas.android.stockhawk.presenter.StockPresenterView;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

import butterknife.BindView;
import butterknife.ButterKnife;


public class StockFragment extends Fragment implements StockPresenterView, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 0;
    public static final String TAG = StockFragment.class.getSimpleName();
    private LoaderManager loaderManager;
    private Intent mServiceIntent;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.message)
    TextView message;

    private Context mContext;
    private ItemTouchHelper mItemTouchHelper;
    private QuoteCursorAdapter mCursorAdapter;
    private Cursor mCursor;
    private StockPresenter presenter;

    public StockFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_stock, container, false);

        mContext = getActivity();

        loaderManager = getLoaderManager();

        presenter = StockPresenterFactory.getStockPresenter(this);

        ButterKnife.bind(this, rootView);

        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately


        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        loaderManager.initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(mContext, null);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(mContext,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        final Cursor cursor = mCursorAdapter.getCursor();
                        cursor.moveToPosition(position);
                        presenter.selectStock(cursor);
                    }
                }));
        recyclerView.setAdapter(mCursorAdapter);

        fab.attachToRecyclerView(recyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                presenter.addStockRequired();

            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        //mTitle = getTitle();

        presenter.startPresenter();

        if (savedInstanceState == null) {
            // Run the initialize task service so that some stocks appear upon an empty database
            presenter.startInitService();

        }

        return rootView;
    }



    @Override
    public void showMessage(boolean showMessage) {
        message.setVisibility(showMessage? View.GONE:View.VISIBLE);
        recyclerView.setVisibility(!showMessage?View.GONE:View.VISIBLE);
    }

    @Override
    public void enableAddStock() {
        new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                .content(R.string.content_test)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // On FAB click, receive user input. Make sure the stock doesn't already exist
                        // in the DB and proceed accordingly
                        Cursor c = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                new String[]{input.toString()}, null);
                        if (c.getCount() != 0) {
                            Toast toast =
                                    Toast.makeText(mContext, R.string.stock_already_saved,
                                            Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                            toast.show();
                            return;
                        } else {
                            // Add the stock to DB
                            presenter.addStock(input);
                        }
                    }
                })
                .show();
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(mContext, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }


    @Override
    public void onResume() {
        super.onResume();
        loaderManager.restartLoader(CURSOR_LOADER_ID, null, this);
    }

    public void networkToast() {
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showAddStockFail(final String s) {
        showMessage(s);
    }

    public void showMessage(String s) {
        Snackbar snackbar = Snackbar
                .make(getView(), s, Snackbar.LENGTH_LONG);

        snackbar.show();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }


}
