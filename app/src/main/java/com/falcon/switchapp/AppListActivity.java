package com.falcon.switchapp;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.falcon.switchapp.ui.main.AppAlternateListAdapter;
import com.falcon.switchapp.ui.main.AppListAdapter;
import com.falcon.switchapp.ui.main.AppListViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.inmobi.ads.AdMetaInfo;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.InMobiNative;
import com.inmobi.ads.listeners.InterstitialAdEventListener;
import com.inmobi.ads.listeners.NativeAdEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppListActivity extends AppCompatActivity implements INetworkDependentActivity{

    private InMobiInterstitial interstitialAd;
    private boolean adLoaded = false;
    private static AppListActivity instance;
    private AppListViewModel mViewModel;
    private AppListAdapter mAdapter;
    private Integer mLastNativeAdLocation = -1;
    private NativeAdEventListener mNativeAdListener;
    private final List<InMobiNative> mNativeAds = new ArrayList<>();
    private final List<InMobiNative> mLoadedNativeAds = new ArrayList<>();
    private InMobiBanner bannerAd;
    private int mNetworkStatus;
    NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver(this);
    private BottomSheetBehavior sheetBehavior;
    private View bottom_sheet;

    private static String SHARED_PREF_KEY = "APPS_DATA";

    public static AppListActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list_activity);

        bottom_sheet = findViewById(R.id.alternateApp);
        bottom_sheet.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        final RecyclerView alternateAppView = findViewById(R.id.alter_app_list);
        alternateAppView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.close_alternateOption).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                alternateAppView.setAdapter(null);
            }
        });
        final RecyclerView recyclerView = findViewById(R.id.app_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        bannerAd = findViewById(R.id.banner);

        interstitialAd = new InMobiInterstitial(AppListActivity.this,
                1593117041651L, new adListener());

        instance = this;
        mViewModel = ViewModelProviders.of(this).get(AppListViewModel.class);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(R.layout.loading_page).setCancelable(false).create();
        dialog.show();
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        mNativeAdListener = new nativeAdListener();
        mViewModel.fetchLatestList(this.getPackageManager(), sharedPref, new AppListViewModel.IOnLoadCallback() {
            @Override
            public void onLoad() {
                findViewById(R.id.app_content).setVisibility(View.VISIBLE);
                dialog.cancel();
                if(mViewModel.getApplist().size() > 0) {
                    mAdapter = new AppListAdapter(mViewModel.getApplist(), instance, new AppListAdapter.IButtonClickAction() {
                        @Override
                        public void onAlternateButtonClicked(ArrayList<AppListViewModel.AlternateAppViewModel> alternateApps) {
                            if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                // specify an adapter (see also next example)
                                AppAlternateListAdapter mAdapter = new AppAlternateListAdapter(alternateApps, instance);
                                alternateAppView.setAdapter(mAdapter);
                            }
                        }
                    });
                    ((TextView)findViewById(R.id.scannedSummaryText)).setText(String.format(instance.getString(R.string.app_summary), mAdapter.getItemCount()));
                    recyclerView.setAdapter(mAdapter);
                    if (mLoadedNativeAds.size()>0) {
                        synchronized (mLastNativeAdLocation) {
                            for (InMobiNative ad : mLoadedNativeAds) {
                                if (mAdapter.getItemCount() > mLastNativeAdLocation+5) {
                                    mAdapter.injectAd(mLastNativeAdLocation+5, new AppListViewModel.DetectedAppViewModel("ad", ad.getAdTitle(), ad.getAdIconUrl(), ad.getAdDescription(), ad.getAdLandingPageUrl()));
                                    mLastNativeAdLocation+=5;
                                }
                            }
                        }
                    }
                } else {
                    findViewById(R.id.no_app_view).setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });

        RadioGroup optionsGroup = findViewById(R.id.countryOptions);
        optionsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.all) {
                    mAdapter.setFilter(AppListViewModel.Country.All);
                } else if (i == R.id.china) {
                    mAdapter.setFilter(AppListViewModel.Country.China);
                } else {
                    Toast.makeText(instance, R.string.coming_soon, Toast.LENGTH_LONG).show();
                }

            }
        } );

        // load ads
        if (mNetworkStatus == NetworkUtil.TYPE_WIFI || mNetworkStatus == NetworkUtil.TYPE_MOBILE) {
            for (int i=0;i<8;i++) {
                InMobiNative nativeAd = new InMobiNative(AppListActivity.this,
                        1590307309081L, mNativeAdListener);
                nativeAd.load();
                mNativeAds.add(nativeAd);
            }
            bannerAd.load();
            interstitialAd.load();
        }
    }

    @Override
    public void onBackPressed() {
        if (adLoaded) {
            interstitialAd.show();
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
            adLoaded = true;
            //interstitialAd.show();
        }

        public void onAdLoadFailed(InMobiInterstitial var1, InMobiAdRequestStatus var2) {
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
            finish();
        }

        public void onRequestPayloadCreated(byte[] var1) {
        }

        public void onRequestPayloadCreationFailed(InMobiAdRequestStatus var1) {
        }
    }

    private class nativeAdListener extends NativeAdEventListener {

        public void onAdLoadSucceeded(InMobiNative ad, AdMetaInfo var2) {
            synchronized (mLastNativeAdLocation) {
                if (mAdapter != null && mAdapter.getItemCount() > mLastNativeAdLocation+5) {
                    mAdapter.injectAd(mLastNativeAdLocation+5, new AppListViewModel.DetectedAppViewModel("ad", ad.getAdTitle(), ad.getAdIconUrl(), ad.getAdDescription(), ad.getAdLandingPageUrl()));
                    mLastNativeAdLocation += 5;
                } else if (mAdapter == null) {
                    mLoadedNativeAds.add(ad);
                }
            }
        }

        public void onAdLoadFailed(InMobiNative ad, InMobiAdRequestStatus requestStatus) {
            Log.d("", "InMobi Init Successful");
        }

    }

    public void onNetworkStatusChanged(int networkStatus) {
        if (networkStatus == NetworkUtil.TYPE_WIFI || networkStatus == NetworkUtil.TYPE_MOBILE) {
            if (bannerAd != null && networkStatus != mNetworkStatus) {
                bannerAd.load();
            }
            if (interstitialAd != null && networkStatus != mNetworkStatus) {
                interstitialAd.load();
            }
        }
        mNetworkStatus = networkStatus;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    public void onPause() {
        this.unregisterReceiver(networkChangeReceiver);
        super.onPause();
    }
}
