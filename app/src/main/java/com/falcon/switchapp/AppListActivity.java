package com.falcon.switchapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.falcon.switchapp.ui.main.AppListAdapter;
import com.falcon.switchapp.ui.main.AppListViewModel;
import com.inmobi.ads.AdMetaInfo;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.listeners.InterstitialAdEventListener;

import java.util.Map;

public class AppListActivity extends AppCompatActivity {

    private InMobiInterstitial interstitialAd;
    private boolean adLoaded = false;
    private static AppListActivity instance;
    private AppListViewModel mViewModel;

    public static AppListActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list_activity);

        final RecyclerView recyclerView = findViewById(R.id.app_list);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        instance = this;
        mViewModel = ViewModelProviders.of(this).get(AppListViewModel.class);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(R.layout.loading_page).setCancelable(false).create();
        mViewModel.fetchLatestList(this.getPackageManager(), new AppListViewModel.IOnLoadCallback() {
            @Override
            public void onLoad() {
                findViewById(R.id.app_content).setVisibility(View.VISIBLE);
                dialog.cancel();
                if(mViewModel.getApplist().size() > 0) {
                    AppListAdapter mAdapter = new AppListAdapter(mViewModel.getApplist(), instance);
                    ((TextView)findViewById(R.id.scannedSummaryText)).setText(String.format(instance.getString(R.string.app_summary), mAdapter.getItemCount()));
                    recyclerView.setAdapter(mAdapter);
                } else {
                    findViewById(R.id.no_app_view).setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });
        dialog.show();
        RadioGroup optionsGroup = findViewById(R.id.countryOptions);
        int checkedButtonId = optionsGroup.getCheckedRadioButtonId();

        if (checkedButtonId == R.id.all) {

        } else if (checkedButtonId == R.id.china) {

        } else {
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_LONG).show();
        }

        InMobiBanner bannerAd = findViewById(R.id.banner);
        bannerAd.load();

        InterstitialAdEventListener mInterstitialAdEventListener = new adListener();
        interstitialAd = new InMobiInterstitial(AppListActivity.this, 1593117041651L, mInterstitialAdEventListener);
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
