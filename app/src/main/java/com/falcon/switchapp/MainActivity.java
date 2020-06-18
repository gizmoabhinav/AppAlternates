package com.falcon.switchapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.sdk.InMobiSdk;
import com.inmobi.sdk.SdkInitializationListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    Activity mActivity;
    private Spinner spinner;
    FloatingActionButton fab;
    private static final String[] paths = {"China", "India", "USA"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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
                    Log.d("", "InMobi Init Successful");
                }
            }
        });
        setContentView(R.layout.activity_main);


        mActivity = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity,
                R.layout.spinner_item, paths);

        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        fab = findViewById(R.id.scanButton);
        final View disabledButtonFrame = findViewById(R.id.disabledButtonFrame);
        final View scanButtonView = findViewById(R.id.ScanButtonFrame);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    disabledButtonFrame.setVisibility(View.VISIBLE);
                    scanButtonView.setVisibility(View.GONE);
                } else {
                    disabledButtonFrame.setVisibility(View.GONE);
                    scanButtonView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        setSupportActionBar(toolbar);

        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);
        final InMobiBanner bannerAd = findViewById(R.id.bannermain);
        bannerAd.load();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                // TODO: KOKO call ad show before this.
                bannerAd.load();
                view.getContext().startActivity(new Intent(mActivity, AppListActivity.class));
            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic("india")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "passed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
