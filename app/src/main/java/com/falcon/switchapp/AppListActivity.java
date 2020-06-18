package com.falcon.switchapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.falcon.switchapp.ui.main.AppListAdapter;
import com.falcon.switchapp.ui.main.AppListViewModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
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

    private static final String[] paths = {"China", "India", "USA"};
    private int selectedCountry = 0;

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
        mViewModel.fetchLatestList(this.getPackageManager(), new AppListViewModel.IOnLoadCallback() {
            @Override
            public void onLoad() {
                if(mViewModel.getApplist().size() > 0) {
                    // specify an adapter (see also next example)
//                    findViewById(R.id.no_app_view).setVisibility(View.GONE);
                    AppListAdapter mAdapter = new AppListAdapter(mViewModel.getApplist(), instance);
                    recyclerView.setAdapter(mAdapter);
                } else {
                    findViewById(R.id.no_app_view).setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }

            }
        });
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, paths);

        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        final View scanButtonView = findViewById(R.id.searchButton);
        scanButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedCountry != 0) {
                    Toast.makeText(instance, getString(R.string.coming_soon), Toast.LENGTH_LONG).show();
                } else {
                }
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCountry = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
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
