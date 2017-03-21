package com.example.soul.exchange_app.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.soul.exchange_app.adapter.CardAdapter;
import com.example.soul.exchange_app.data.ExchangeData;
import com.example.soul.exchange_app.paser.AsyncResponse;
import com.example.soul.exchange_app.paser.ExchangeInfo;
import com.example.soul.exchange_app.paser.ExchangeParser;
import com.example.soul.exchange_app.util.DateUtil;

import java.util.List;
import java.util.concurrent.Callable;


/**
 * Created by soul on 2017. 2. 26..
 */

public class OneFragmentManager implements ExchangeInfo, AsyncResponse {

    private final String TAG = getClass().getSimpleName();
    private DataAsync dataAsync;
    private ExchangeParser exchangeParser;
    private List<ExchangeData> exchangeDataList;
    private CardAdapter adapter;

     public DataAsync excuteDataAsync(RecyclerView recyclerView, View viewExchange, SwipeRefreshLayout mSwipeRefreshLayout,TextView dateUpdateText){
        dataAsync = new DataAsync();
        dataAsync.setInit(recyclerView, viewExchange, mSwipeRefreshLayout, dateUpdateText);
        dataAsync.delegate = this;
        dataAsync.execute();

         return dataAsync;
    }

    public class DataAsync extends AsyncTask<String, Void, List<ExchangeData>> {
        private View viewExchange;
        private RecyclerView recyclerView;
        private SwipeRefreshLayout mSwipeRefreshLayout;
        private TextView dateUpdateText;
        private DateUtil dateUtil;
        public AsyncResponse delegate = null;

        public void setInit(RecyclerView recyclerView, View viewExchange, SwipeRefreshLayout mSwipeRefreshLayout, TextView dateUpdateText){
            this.recyclerView = recyclerView;
            this.viewExchange = viewExchange;
            this.dateUpdateText = dateUpdateText;
            this.mSwipeRefreshLayout = mSwipeRefreshLayout;

            Log.d(TAG, "Is exchangeParser null? >> "+(exchangeParser == null));
            if(exchangeParser == null){
                exchangeParser = new ExchangeParser();
            }

            dateUtil = new DateUtil(viewExchange.getContext());
        }


        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected List<ExchangeData> doInBackground(String... params) {
            exchangeDataList = exchangeParser.getParserDatas();
            return exchangeDataList;
        }


        /**
         * @param mExchangeDatas
         *
         * soution about animation reference is :
         * http://stackoverflow.com/questions/27300811/recyclerview-adapter-notifydatasetchanged-stops-fancy-animation
         */

        @Override
        protected void onPostExecute(List<ExchangeData> mExchangeDatas) {
            adapter = new CardAdapter(viewExchange.getContext(), mExchangeDatas, recyclerView);
            adapter.setHasStableIds(true);
            recyclerView.setAdapter(adapter);
            dateUpdateText.setText(dateUtil.getDate());
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(viewExchange, "Update Data", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            delegate.processFinish(mExchangeDatas);
        }
    }

    @Override
    public void processFinish(List<ExchangeData> mExchangeDatas) {
        Log.d(TAG, "데이터 확인 mExchangeDatas.size : "+mExchangeDatas.size());
    }



    public interface AsyncCallback<T> {
        public void onResult(T result);
        public void exceptionOccured(Exception e);
        public void cancelled();
    }

    public interface AsyncExecutorAware<T> {
        public void setAsyncExecutor(AsyncExecutor<T> asyncExecutor);
    }

    public class AsyncExecutor<T> extends AsyncTask<Void, Void, T> {
        private static final String TAG = "AsyncExecutor";

        private AsyncCallback<T> callback;
        private Callable<T> callable;
        private Exception occuredException;

        private View viewExchange;
        private RecyclerView recyclerView;
        private SwipeRefreshLayout mSwipeRefreshLayout;
        private TextView dateUpdateText;
        private DateUtil dateUtil;

        public void setInit(RecyclerView recyclerView, View viewExchange, SwipeRefreshLayout mSwipeRefreshLayout, TextView dateUpdateText){
            this.recyclerView = recyclerView;
            this.viewExchange = viewExchange;
            this.dateUpdateText = dateUpdateText;
            this.mSwipeRefreshLayout = mSwipeRefreshLayout;

            Log.d(TAG, "Is exchangeParser null? >> "+(exchangeParser == null));
            if(exchangeParser == null){
                exchangeParser = new ExchangeParser();
            }
            dateUtil = new DateUtil(viewExchange.getContext());
        }


        public AsyncExecutor<T> setCallable(Callable<T> callable) {
            this.callable = callable;
            return this;
        }

        public AsyncExecutor<T> setCallback(AsyncCallback<T> callback) {
            this.callback = callback;
            processAsyncExecutorAware(callback);
            return this;
        }

        @SuppressWarnings("unchecked")
        private void processAsyncExecutorAware(AsyncCallback<T> callback) {
            if (callback instanceof AsyncExecutorAware) {
                ((AsyncExecutorAware<T>) callback).setAsyncExecutor(this);
            }
        }

        @Override
        protected T doInBackground(Void... params) {
            try {
                return callable.call();
            } catch (Exception ex) {
                Log.e(TAG,
                        "exception occured while doing in background: "
                                + ex.getMessage(), ex);
                this.occuredException = ex;
                return null;
            }
        }

        @Override
        protected void onPostExecute(T result) {
            if (isCancelled()) {
                notifyCanceled();
            }
            if (isExceptionOccured()) {
                notifyException();
                return;
            }
            notifyResult(result);
        }

        private void notifyCanceled() {
            if (callback != null)
                callback.cancelled();
        }

        private boolean isExceptionOccured() {
            return occuredException != null;
        }

        private void notifyException() {
            if (callback != null)
                callback.exceptionOccured(occuredException);
        }

        private void notifyResult(T result) {
            adapter = new CardAdapter(viewExchange.getContext(), (List<ExchangeData>)result, recyclerView);
            adapter.setHasStableIds(true);
            recyclerView.setAdapter(adapter);
            dateUpdateText.setText(dateUtil.getDate());
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(viewExchange, "Update Data", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            if (callback != null)
                callback.onResult(result);
        }

    }


    public void load() {
        // 비동기로 실행될 코드List<ExchangeData> mExchangeDatas
        Callable<List<ExchangeData>> callable = new Callable<List<ExchangeData>>() {
            @Override
            public List<ExchangeData> call() throws Exception {
                exchangeDataList = exchangeParser.getParserDatas();
                return exchangeDataList;
            }
        };

        new AsyncExecutor<List<ExchangeData>>()
                .setCallable(callable)
                .setCallback(callback)
                .execute();
    }

    // 비동기로 실행된 결과를 받아 처리하는 코드
    private AsyncCallback<List<ExchangeData>> callback = new AsyncCallback<List<ExchangeData>>() {
        @Override
        public void onResult(List<ExchangeData> result) {

        }

        @Override
        public void exceptionOccured(Exception e) {

        }

        @Override
        public void cancelled() {
        }
    };

}
