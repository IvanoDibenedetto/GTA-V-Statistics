package com.makeitsimple.gtav_onlinestats;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;

public class ConnectionHelper {

   /* public static boolean isConnected(Context context){
        ConnectivityManager manager =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI){
                //we have WIFI
                return true;
            }
            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                //we have cellular data
                return true;
            }
        }
        return false;
    }*/

    public static boolean isConnected() throws InterruptedException, IOException {
        final String command = "ping -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }
}
