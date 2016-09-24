package com.example.bublovskiy.project1_july;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by BMW on 2016-08-02.
 */
public class isMyServiceRunning {

    static Context context;

    //constructor with context initiating
    isMyServiceRunning(Context context) {
        this.context = context;
    }

    //check if specific service is already running
    public static boolean isRunning (Class myClass) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        //get all services who are currently running and compare to requested service
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (myClass.getName().equals(service.service.getClassName())) {
                //true is the service is already running
                return true;
            }
        }

        //false is the service is not yet running
        return false;
    }//end isRunning

}//end isMyServiceRunning
