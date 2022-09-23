package com.dave.soul.exchange_app.view.service

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class BackupWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        // call methods to perform background task
        Log.i(TAG, "do worker@@@@@@@@@@@@@")
        return Result.success()
    }

    companion object {
        private const val TAG = "BackupWorker"
    }
}
