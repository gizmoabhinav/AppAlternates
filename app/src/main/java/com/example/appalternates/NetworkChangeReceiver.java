package com.example.appalternates;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.appalternates.ui.main.MainFragment;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);
        Log.e("network reciever", "network change");
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                try{
                    AppListActivity.getInstance().loadAd();
                    MainFragment.getInstance().loadAd();
                } catch (Exception e) {

                }
            } else {

            }
        }
    }
}
