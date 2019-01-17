package com.dave.soul.exchange_app.util

import android.util.Log
import com.dave.soul.exchange_app.BuildConfig

/**
 * Created by soul on 2017. 11. 10..
 */
object DLog {

    private val TAG : String = "Dave"

    fun e(message : String){
        if(BuildConfig.DEBUG) Log.e(TAG, buildLogMsg(message))
    }

    fun w(message : String){
        if(BuildConfig.DEBUG) Log.w(TAG, buildLogMsg(message))
    }

    fun i(message : String){
        if(BuildConfig.DEBUG) Log.i(TAG, buildLogMsg(message))
    }

    fun d(message : String){
        if(BuildConfig.DEBUG) Log.d(TAG, buildLogMsg(message))
    }

    fun v(message : String){
        if(BuildConfig.DEBUG) Log.v(TAG, buildLogMsg(message))
    }

    private fun buildLogMsg(message : String) : String{
        val ste : StackTraceElement ?= Thread.currentThread().stackTrace[4]
        val sb = StringBuilder()
        sb.append("[")
        sb.append(ste?.fileName?.replace(".java", ""))
        sb.append("::")
        sb.append(ste?.methodName)
        sb.append("]")
        sb.append(message)
        return sb.toString()
    }
}
