package com.dave.soul.exchange_app.view.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.dave.soul.exchange_app.R;
import com.dave.soul.exchange_app.view.adapter.DialogAdapter;
import com.dave.soul.exchange_app.realm.RealmController;

import io.realm.Realm;

/**
 * Created by soul on 2017. 5. 18..
 */

public class CountryDialog extends DialogFragment {
    private RecyclerView mRecyclerView;
    private DialogAdapter adapter;
    private Realm realm;
    private DialogAdapter.OnItemClickListener mListener;

    public CountryDialog(){}

    @SuppressLint("ValidFragment")
    public CountryDialog(DialogAdapter.OnItemClickListener mListener){
        this.mListener = mListener;
    }

    // this method create view for your Dialog
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog, container, false);

        realm = Realm.getDefaultInstance();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_dialog);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //setadapter
        adapter = new DialogAdapter(RealmController.getExchangeRate(realm), getActivity(), mListener);
        mRecyclerView.setAdapter(adapter);
        //get your recycler view and populate it.
        return v;
    }

    public void onResume()
    {
        super.onResume();

        // resize popup
        DisplayMetrics dm = getActivity().getResources().getDisplayMetrics();
        int width   = (int) (dm.widthPixels*0.9);
        int height  = (int) (dm.heightPixels/0.9);

        Window window = getDialog().getWindow();
        window.setLayout(width, height);
        window.setGravity(Gravity.CENTER);
    }
}
