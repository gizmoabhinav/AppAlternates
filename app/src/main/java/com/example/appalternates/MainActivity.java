package com.example.appalternates;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.sdk.InMobiSdk;
import com.inmobi.sdk.SdkInitializationListener;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    Activity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActivity = this;
        FloatingActionButton fab = findViewById(R.id.scanButton);
        JSONObject consentObject = new JSONObject();
        /*
        try {
            // Provide correct consent value to sdk which is obtained by User
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true);
            // Provide 0 if GDPR is not applicable and 1 if applicable
            consentObject.put("gdpr", "0");
            // Provide user consent in IAB format
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_IAB, “<<consent in IAB format>>”);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        InMobiSdk.init(this, "ed497874941c44d8aaa340a5a8a2ae99", consentObject, new SdkInitializationListener() {
            @Override
            public void onInitializationComplete(@Nullable Error error) {
                if (null != error) {
                    Log.e("", "InMobi Init failed -" + error.getMessage());
                } else {
                    InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);
                    InMobiBanner bannerAd = findViewById(R.id.bannermain);
                    bannerAd.load();
                    Log.d("", "InMobi Init Successful");
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: KOKO call ad show before this.
                view.getContext().startActivity(new Intent(mActivity, AppListActivity.class));
            }
        });
    }

}
