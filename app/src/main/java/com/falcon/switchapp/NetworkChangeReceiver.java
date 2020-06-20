package com.falcon.switchapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {

    INetworkDependentActivity activity;

    public  NetworkChangeReceiver() {}

    NetworkChangeReceiver(INetworkDependentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction()) ||
                "android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction()) ||
                "android.net.wifi.supplicant.STATE_CHANGE".equals(intent.getAction())) {
            if (activity != null) {
                activity.onNetworkStatusChanged(status);
            }
        }
    }
}
