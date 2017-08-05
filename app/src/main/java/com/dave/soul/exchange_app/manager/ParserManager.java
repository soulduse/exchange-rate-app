package com.dave.soul.exchange_app.manager;

import android.os.AsyncTask;
import android.util.Log;

import com.dave.soul.exchange_app.paser.ExchangeInfo;

import java.util.concurrent.Callable;

/**
 * Created by soul on 2017. 4. 30..
 */

public class ParserManager implements ExchangeInfo {

    private final String TAG = getClass().getSimpleName();

    public ParserManager.AsyncExecutor getAsyncExecutor(){
        return new ParserManager.AsyncExecutor();
    }

    public interface AsyncCallback<T> {
        void onResult(T result);
        void exceptionOccured(Exception e);
        void cancelled();
    }

    public interface AsyncExecutorAware<T> {
        void setAsyncExecutor(ParserManager.AsyncExecutor<T> asyncExecutor);
    }

    public class AsyncExecutor<T> extends AsyncTask<T, Void, T> {
        private final String TAG = this.getClass().getSimpleName();

        private ParserManager.AsyncCallback<T> callback;
        private Callable<T> callable;
        private Exception occuredException;

        public ParserManager.AsyncExecutor<T> setCallable(Callable<T> callable) {
            this.callable = callable;
            return this;
        }

        public ParserManager.AsyncExecutor<T> setCallback(ParserManager.AsyncCallback<T> callback) {
            this.callback = callback;
            processAsyncExecutorAware(callback);
            return this;
        }

        @SuppressWarnings("unchecked")
        private void processAsyncExecutorAware(ParserManager.AsyncCallback<T> callback) {
            if (callback instanceof ParserManager.AsyncExecutorAware) {
                ((ParserManager.AsyncExecutorAware<T>) callback).setAsyncExecutor(this);
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
            if (callback != null)
                callback.onResult(result);
        }
    }
}
