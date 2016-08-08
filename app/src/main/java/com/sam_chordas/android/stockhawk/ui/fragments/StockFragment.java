package com.sam_chordas.android.stockhawk.ui.fragments;


import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
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


public class StockFragment extends Fragment implements StockPresenterView {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.no_network)
    TextView noNetwork;

    @BindView(R.id.out_of_date)
    TextView outOfDate;

    private Context mContext;
    private ItemTouchHelper mItemTouchHelper;
    private QuoteCursorAdapter mCursorAdapter;
    private StockPresenter presenter;

    public StockFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_stock, container, false);

        mContext = getActivity();

        presenter = StockPresenterFactory.getStockPresenter(this);

        ButterKnife.bind(this, rootView);

        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mCursorAdapter = new QuoteCursorAdapter(mContext, null, presenter);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(mContext,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        final Cursor cursor = mCursorAdapter.getCursor();
                        cursor.moveToPosition(position);
                        presenter.selectStock(cursor, v);
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
    public void getStockName() {
        new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                .content(R.string.content_test)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, final CharSequence input) {
                        new Thread(){
                            @Override
                            public void run() {
                                // On FAB click, receive user input. Make sure the stock doesn't already exist
                                // in the DB and proceed accordingly
                                Cursor c = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                        new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                        new String[]{input.toString()}, null);
                                if (c.getCount() != 0) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast toast =
                                                    Toast.makeText(mContext, R.string.stock_already_saved,
                                                            Toast.LENGTH_LONG);
                                            toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                                            toast.show();
                                        }
                                    });
                                    return;
                                } else {
                                    // Add the stock to DB
                                    presenter.addStock(input);
                                }
                            }
                        }.start();
                    }
                })
                .show();
    }


    public void networkToast() {
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showAddStockFail(final String s) {
        showMessage(s);
    }

    @Override
    public void resetData() {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void setData(Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void showStockList() {
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showDatedMessage() {
        outOfDate.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideDatedMessage() {
        outOfDate.setVisibility(View.GONE);
    }

    @Override
    public void hideStockList() {
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void hideAddStock() {
        fab.setVisibility(View.GONE);
    }

    @Override
    public void enableAddStock() {
        fab.setVisibility(View.VISIBLE);
    }

    @Override
    public void showNoNetworkMessage() {
        noNetwork.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoNetworkMessage() {
        noNetwork.setVisibility(View.GONE);
    }

    public void showMessage(String s) {
        Snackbar snackbar = Snackbar
                .make(getView(), s, Snackbar.LENGTH_LONG);

        snackbar.show();
    }


}
