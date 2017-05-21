package com.example.soul.exchange_app.manager;

import android.os.AsyncTask;
import android.util.Log;

import com.example.soul.exchange_app.paser.ExchangeInfo;

import java.util.concurrent.Callable;

import io.realm.Realm;

/**
 * Created by soul on 2017. 4. 30..
 */

public class DataManager implements ExchangeInfo {

    private final String TAG = getClass().getSimpleName();

    public DataManager.AsyncExecutor getAsyncExecutor(){
        return new DataManager.AsyncExecutor();
    }

    public interface AsyncCallback<T> {
        void onResult(T result);
        void exceptionOccured(Exception e);
        void cancelled();
    }

    public interface AsyncExecutorAware<T> {
        void setAsyncExecutor(DataManager.AsyncExecutor<T> asyncExecutor);
    }

    public class AsyncExecutor<T> extends AsyncTask<T, Void, T> {
        private final String TAG = this.getClass().getSimpleName();

        private DataManager.AsyncCallback<T> callback;
        private Callable<T> callable;
        private Exception occuredException;

        public DataManager.AsyncExecutor<T> setCallable(Callable<T> callable) {
            this.callable = callable;
            return this;
        }

        public DataManager.AsyncExecutor<T> setCallback(DataManager.AsyncCallback<T> callback) {
            this.callback = callback;
            processAsyncExecutorAware(callback);
            return this;
        }

        @SuppressWarnings("unchecked")
        private void processAsyncExecutorAware(DataManager.AsyncCallback<T> callback) {
            if (callback instanceof DataManager.AsyncExecutorAware) {
                ((DataManager.AsyncExecutorAware<T>) callback).setAsyncExecutor(this);
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
