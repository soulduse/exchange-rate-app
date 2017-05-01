package com.example.soul.exchange_app.activity;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.adapter.CardAdapter;
import com.example.soul.exchange_app.manager.DataManager;
import com.example.soul.exchange_app.manager.OneFragmentManager;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.paser.ExchangeParser;
import com.example.soul.exchange_app.realm.RealmController;
import com.example.soul.exchange_app.util.DateUtil;
import com.example.soul.exchange_app.util.NetworkUtil;

import java.util.List;
import java.util.concurrent.Callable;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by soul on 2017. 2. 24..
 */

public class OneFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    // view
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView dateUpdateText;
    private View view;
    private CardAdapter adapter;

    // data
    private OneFragmentManager oneFragmentManager;
    private DataManager dataManager;
    private DateUtil dateUtil;

    // Realm
    private Realm realm;
    private RealmController realmController;
    private RealmResults<ExchangeRate> copyRealmResults;

    public OneFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // data initialization
        oneFragmentManager  = new OneFragmentManager();
        dataManager = new DataManager();
        dateUtil    = new DateUtil(getContext());
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (view != null)
        {
            ViewGroup parent = (ViewGroup)view.getParent();
            parent.removeView(view);
        } else {
            view = inflater.inflate(R.layout.fragment_one, container, false);
        }
        recyclerView    = (RecyclerView)view.findViewById(R.id.recycler_view_frag_one);
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_layout);
        dateUpdateText = (TextView)view.findViewById(R.id.text_view_update_date);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 1);
        mLayoutManager.supportsPredictiveItemAnimations();
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

//        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);


//        realmController = RealmController.with(getContext());
//        this.realm = realmController.getRealm();

        load();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                load();
            }
        });

        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public void load() {
        // 비동기로 실행될 코드List<ExchangeRate> mExchangeDatas
        Callable<List<ExchangeRate>> callable = new Callable<List<ExchangeRate>>() {
            @Override
            public List<ExchangeRate> call() throws Exception {
                return getParserDataList();
            }
        };

//        oneFragmentManager.getAsyncExecutor()
//                .setInit(recyclerView, view, mSwipeRefreshLayout, dateUpdateText)
//                .setCallable(callable)
//                .setCallback(callback)
//                .execute();

        dataManager.getAsyncExecutor()
                .setCallable(callable)
                .setCallback(callback)
                .execute();
    }

    /**
        네트워크 연결상태에 따른 어디서 데이터를 가져올 것인가에 대한 구분 (두 가지 경우의 수가 있다.)
        - network connect       : parsing data를 가져온다.
        - network disconnect    : Realm DB에서 내용을 가져온다.
     */
    private List<ExchangeRate> getParserDataList(){
        realmController = RealmController.with(getContext());
        realm = realmController.getRealm();
        List<ExchangeRate> exchangeRateList = null;

        /*
        if(NetworkUtil.isNetworkConnected(getContext())){
            // connected network    - getData from parsing
//            exchangeRateList = new ExchangeParser().getParserDatas();

            // if network is connected, realm database will change datas
            realmController.setRealmDatas(new ExchangeParser().getParserDatas());
            exchangeRateList = realmController.getExchangeRate();
            Log.d(TAG, "exchangeRateList size : "+exchangeRateList.size());
        }else{
            // disconnected network - getData from realmDB
            try{
//                exchangeRateList = realmController.getExchangeRate().subList(0, realmController.getExchangeRate().size());
                exchangeRateList = realmController.getExchangeRate();
                Log.d(TAG, "exchangeRateList Realm Data Size : "+exchangeRateList.size());
            }catch (NullPointerException ne){
                Snackbar.make(view, "네트워크 연결을 체크 해주세요.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
        */
        if(NetworkUtil.isNetworkConnected(getContext())){
            // connected network    - getData from parsing
            realmController.setRealmDatas(new ExchangeParser().getParserDatas());
        }            // disconnected network - getData from realmDB
        try{
            exchangeRateList = realmController.getExchangeRate();
            Log.d(TAG, "exchangeRateList size : "+exchangeRateList.size());
        }catch (NullPointerException ne){
            Snackbar.make(view, "네트워크 연결을 체크 해주세요.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        return exchangeRateList;
    }



    // 비동기로 실행된 결과를 받아 처리하는 코드
    private DataManager.AsyncCallback<List<ExchangeRate>> callback = new DataManager.AsyncCallback<List<ExchangeRate>>() {
        @Override
        public void onResult(List<ExchangeRate> result) {
            adapter = new CardAdapter(copyRealmResults);
            adapter.setHasStableIds(true);
            recyclerView.setAdapter(adapter);
            dateUpdateText.setText(dateUtil.getDate());
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(view, "Update Data", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        }

        @Override
        public void exceptionOccured(Exception e) {
            Log.d(TAG, "exceptionOccured : "+e.getMessage());
        }

        @Override
        public void cancelled() {
            Log.d(TAG, "cancelled");
        }
    };

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration{
        private int spanCount;
        private int spacting;
        private boolean includeEdge;


        public GridSpacingItemDecoration(int spanCount, int spacting, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacting = spacting;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;  // item column

            if(includeEdge){
                outRect.left = spacting - column * spacting / spanCount;    // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacting / spanCount;

                if(position < spanCount){
                    outRect.top = spacting;
                }
                outRect.top = spacting;
            }else{
                outRect.left = column * spacting / spanCount;
                outRect.right = spacting - (column + 1) * spacting / spanCount;
                if(position >= spanCount){
                    outRect.top = spacting;
                }
            }
        }
    }

    private int dpToPx(int dp){
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


}
