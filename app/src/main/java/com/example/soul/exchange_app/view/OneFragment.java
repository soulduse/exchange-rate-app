package com.example.soul.exchange_app.view;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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

/**
 * Created by soul on 2017. 2. 24..
 */

public class OneFragment extends Fragment {

    // view
    private Button parserBtn;
    private TextView reaserchTxt;
    private RecyclerView recyclerView;

    // data
    private OneFragmentManager oneFragmentManager;
    private ExchangeParser exchangeParser;


    public OneFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // data initialization
        oneFragmentManager = new OneFragmentManager();
        exchangeParser = new ExchangeParser();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_one, container, false);
        parserBtn       = (Button)view.findViewById(R.id.parser_btn);
//        reaserchTxt     = (TextView)view.findViewById(R.id.reaserch_text);
        recyclerView    = (RecyclerView)view.findViewById(R.id.recycler_view_frag_one);



        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.setAdapter(adapter);

        oneFragmentManager.excuteDataAsync(recyclerView, view);

        parserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(reaserchTxt != null && !reaserchTxt.getText().equals("")){
                    reaserchTxt.setText("");
                }
                oneFragmentManager.excuteDataAsync(recyclerView, view);
//                reaserchTxt.setText(exchangeDataParser.getParserString());
//                List<String> perCountryList = paser.getParserList();
//                for(int i=0; i<perCountryList.size(); i++){
//                    perCountryList.get(i);
//                }
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
