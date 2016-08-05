package com.sam_chordas.android.stockhawk.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sam_chordas.android.stockhawk.R;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailFragment extends Fragment {
    public static final String STOCK_ID = "stock_id";
    private String stockId;

    @BindView(R.id.graph)
    ImageView graph;

    public StockDetailFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stockId = getArguments().getString(STOCK_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_stock_detail, container, false);

        ButterKnife.bind(this, view);

        final String imgPath = String.format("http://chart.finance.yahoo.com/z?s=%s&t=6m&q=l&l=on&z=l&p=m50,m200", stockId);
        Picasso.with(getActivity()).load(imgPath).into(graph);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}
