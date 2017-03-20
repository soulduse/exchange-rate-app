package com.example.soul.exchange_app.view;

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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.manager.OneFragmentManager;
import com.example.soul.exchange_app.paser.ExchangeParser;
import com.example.soul.exchange_app.util.DateUtil;

import org.w3c.dom.Text;

/**
 * Created by soul on 2017. 2. 24..
 */

public class OneFragment extends Fragment {

    // view
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView dateUpdateText;
    private View view;

    // data
    private OneFragmentManager oneFragmentManager;


    public OneFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // data initialization
        oneFragmentManager = new OneFragmentManager();
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
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);

        oneFragmentManager.excuteDataAsync(recyclerView, view, mSwipeRefreshLayout, dateUpdateText);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){

            @Override
            public void onRefresh() {
                oneFragmentManager.excuteDataAsync(recyclerView, view, mSwipeRefreshLayout, dateUpdateText);
            }
        });

        return view;
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
}
