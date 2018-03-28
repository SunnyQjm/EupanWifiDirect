
package com.j.ming.eupanwifidirect.util;

import android.content.Context;
import android.net.wifi.WifiManager;

public class NetworkUtils {
    public static boolean setWifiEnable(Context context, boolean enable){
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wm == null)
            return false;
        if(enable != wm.isWifiEnabled()){
            wm.setWifiEnabled(enable);
        }
        return wm.isWifiEnabled();
    }
}