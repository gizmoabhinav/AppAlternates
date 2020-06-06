package com.example.appalternates;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appalternates.ui.main.AppAlternateListAdapter;
import com.example.appalternates.ui.main.AppListViewModel;
import com.inmobi.ads.AdMetaInfo;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.listeners.InterstitialAdEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppAlternateListActivity extends AppCompatActivity {

    private InMobiInterstitial interstitialAd;
    private boolean adLoaded = false;
    private static AppAlternateListActivity instance;

    public static AppAlternateListActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_alternate_list_activity);
        RecyclerView recyclerView = findViewById(R.id.app_list);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        List<AppListViewModel.AlternateAppViewModel> mAppList = intent.getParcelableArrayListExtra("alternateApps");
        instance = this;

        // specify an adapter (see also next example)
        AppAlternateListAdapter mAdapter = new AppAlternateListAdapter(mAppList, instance);
        recyclerView.setAdapter(mAdapter);

        InMobiBanner bannerAd = findViewById(R.id.banner);
        bannerAd.load();
        InterstitialAdEventListener mInterstitialAdEventListener = new adListener();
        interstitialAd = new InMobiInterstitial(AppAlternateListActivity.this, 1593117041651L, mInterstitialAdEventListener);
        interstitialAd.load();
    }

    @Override
    public void onBackPressed() {
        if (adLoaded) {
//            interstitialAd.show();
        } else {
            super.onBackPressed();
        }
    }

    public void loadAd() {
        if (!adLoaded) {
            interstitialAd.load();
        }
    }

    private class adListener extends InterstitialAdEventListener {
        public adListener() {
        }

        public void onAdLoadSucceeded(InMobiInterstitial var1, AdMetaInfo var2) {
            //interstitialAd.show();
        }

        public void onAdLoadFailed(InMobiInterstitial var1, InMobiAdRequestStatus var2) {
            Log.d("", "InMobi Init Successful");
        }

        public void onAdReceived(InMobiInterstitial var1) {
            Log.d("", "InMobi Init Successful");
        }

        public void onAdClicked(InMobiInterstitial var1, Map<Object, Object> var2) {
        }

        public void onAdWillDisplay(InMobiInterstitial var1) {
        }

        public void onAdDisplayed(InMobiInterstitial var1) {
        }

        public void onAdDisplayFailed(InMobiInterstitial var1) {
            finish();
        }

        public void onAdDismissed(InMobiInterstitial var1) {
            finish();
        }

        public void onUserLeftApplication(InMobiInterstitial var1) {
        }

        public void onRewardsUnlocked(InMobiInterstitial var1, Map<Object, Object> var2) {
            //finish();
        }

        public void onRequestPayloadCreated(byte[] var1) {
        }

        public void onRequestPayloadCreationFailed(InMobiAdRequestStatus var1) {
        }
    }
}
