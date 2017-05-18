package com.example.soul.exchange_app.activity;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.adapter.DialogAdapter;
import com.example.soul.exchange_app.realm.RealmController;

import io.realm.Realm;

/**
 * Created by soul on 2017. 5. 18..
 */

public class MyDialogFragment extends DialogFragment {
    private RecyclerView mRecyclerView;
    private DialogAdapter adapter;
    private Realm realm;
    private RealmController realmController;

    // this method create view for your Dialog
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //inflate layout with recycler view
        View v = inflater.inflate(R.layout.fragment_dialog, container, false);

        realm = Realm.getDefaultInstance();
        realmController = RealmController.getInstance();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_dialog);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //setadapter
        adapter = new DialogAdapter(realmController.getExchangeRateExceptKorea(), getActivity());
        mRecyclerView.setAdapter(adapter);
        //get your recycler view and populate it.
        return v;
    }
}
