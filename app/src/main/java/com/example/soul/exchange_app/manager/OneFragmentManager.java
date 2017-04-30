package com.example.soul.exchange_app.manager;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.soul.exchange_app.adapter.CardAdapter;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.paser.ExchangeInfo;
import com.example.soul.exchange_app.paser.ExchangeParser;
import com.example.soul.exchange_app.util.DateUtil;

import java.util.List;
import java.util.concurrent.Callable;


/**
 * Created by soul on 2017. 2. 26..
 */

public class OneFragmentManager implements ExchangeInfo {

    private final String TAG = getClass().getSimpleName();
    private ExchangeParser exchangeParser;
    private CardAdapter adapter;

    public AsyncExecutor getAsyncExecutor(){
        return new AsyncExecutor();
    }

    public interface AsyncCallback<T> {
        void onResult(T result);
        void exceptionOccured(Exception e);
        void cancelled();
    }

    public interface AsyncExecutorAware<T> {
        void setAsyncExecutor(AsyncExecutor<T> asyncExecutor);
    }

    public class AsyncExecutor<T> extends AsyncTask<T, Void, T> {
        private static final String TAG = "AsyncExecutor";

        private AsyncCallback<T> callback;
        private Callable<T> callable;
        private Exception occuredException;

        private View viewExchange;
        private RecyclerView recyclerView;
        private SwipeRefreshLayout mSwipeRefreshLayout;
        private TextView dateUpdateText;
        private DateUtil dateUtil;

        public AsyncExecutor<T> setInit(RecyclerView recyclerView, View viewExchange, SwipeRefreshLayout mSwipeRefreshLayout, TextView dateUpdateText){
            this.recyclerView = recyclerView;
            this.viewExchange = viewExchange;
            this.dateUpdateText = dateUpdateText;
            this.mSwipeRefreshLayout = mSwipeRefreshLayout;

            Log.d(TAG, "Is exchangeParser null? >> "+(exchangeParser == null));
            if(exchangeParser == null){
                exchangeParser = new ExchangeParser();
            }
            dateUtil = new DateUtil(viewExchange.getContext());
            return this;
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
        protected T doInBackground(T... params) {
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

        /**
         * soution about animation reference is :
         * http://stackoverflow.com/questions/27300811/recyclerview-adapter-notifydatasetchanged-stops-fancy-animation
         */
        private void notifyResult(T result) {
            Log.d(TAG, "entered notifyResult!");
            adapter = new CardAdapter(viewExchange.getContext(), (List<ExchangeRate>)result, recyclerView);
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
}
