package com.dave.soul.exchange_app.activity;

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

import com.dave.soul.exchange_app.R;
import com.dave.soul.exchange_app.adapter.CardAdapter;
import com.dave.soul.exchange_app.manager.ParserManager;
import com.dave.soul.exchange_app.manager.DataManager;
import com.dave.soul.exchange_app.paser.ExchangeParser;
import com.dave.soul.exchange_app.realm.RealmController;
import com.dave.soul.exchange_app.util.DateUtil;

import io.realm.Realm;

/**
 * Created by soul on 2017. 2. 24..
 */

public class OneFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    // view
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView dateUpdateText, noneItemText;
    private View view;
    private CardAdapter adapter;

    // data
    private ParserManager parserManager;
    private DateUtil dateUtil;
    private ExchangeParser ep;

    // Realm
    private Realm realm;

    public OneFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        visibleTextNoneItems();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // data initialization
        parserManager = new ParserManager();
        dateUtil    = DateUtil.getInstance();
        dateUtil.init(getContext());
        ep = new ExchangeParser();
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        realm = Realm.getDefaultInstance();

        view = inflater.inflate(R.layout.fragment_one, container, false);

        recyclerView    = (RecyclerView)view.findViewById(R.id.recycler_view_frag_one);
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_layout);
        dateUpdateText = (TextView)view.findViewById(R.id.text_view_update_date);
        noneItemText = (TextView)view.findViewById(R.id.text_notice_none_items);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 1);
        mLayoutManager.supportsPredictiveItemAnimations();
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);

        setCardAdapter();
        setRefreshText();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                boolean parser = DataManager.newInstance(getActivity()).load();
                if(!parser){
                    printSnackbar(getString(R.string.disconnect_internet));
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        return view;

    }


    @Override
    public void onDestroyView() {
        realm.close();
        super.onDestroyView();
    }

    public void setRefreshText(){
        String s = RealmController.getExchangeDate(realm);
        if(s != null)
            dateUpdateText.setText(s);
    }

    private void setCardAdapter(){
        adapter = new CardAdapter(RealmController.getCheckedItems(realm), getContext());
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    // 관심 환율이 없을 경우 사용자에게 등록된 아이템이 없음을 알린다.
    private void visibleTextNoneItems(){
        Log.d(TAG, "adapter count ==> "+adapter.getItemCount());
        if(adapter.getItemCount() == 0){
            noneItemText.setVisibility(View.VISIBLE);
            dateUpdateText.setVisibility(View.GONE);
        }else{
            noneItemText.setVisibility(View.GONE);
            dateUpdateText.setVisibility(View.VISIBLE);
        }
    }

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

    private void printSnackbar(String msg){
        Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

}
