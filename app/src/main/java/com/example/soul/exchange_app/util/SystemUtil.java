package com.example.soul.exchange_app.util;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;

/**
 * Created by soul on 2017. 4. 30..
 */

public class SystemUtil {

    public static boolean isRunningProcess(Context context, String packageName) {

        boolean isRunning = false;

        ActivityManager actMng = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> list = actMng.getRunningAppProcesses();

        for(ActivityManager.RunningAppProcessInfo rap : list)
        {
            if(rap.processName.equals(packageName))
            {
                isRunning = true;
                break;
            }
        }

        return isRunning;
    }

    public boolean isRunningActivity(Context context, String packageName){
        boolean result = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info;
        info = activityManager.getRunningTasks(7);
        for (Iterator iterator = info.iterator(); iterator.hasNext();)  {
            ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo) iterator.next();
            if(runningTaskInfo.topActivity.getClassName().equals(packageName)) {
                Log.e("ABCApplication","ABCApplication is running");
                result = true;
            }
        }

        return result;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            return !ipAddr.equals("");
        } catch (Exception e) {
            return false;
        }
    }
}
