package com.falcon.switchapp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.sdk.InMobiSdk;
import com.inmobi.sdk.SdkInitializationListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        JSONObject consentObject = new JSONObject();

        try {
            // Provide correct consent value to sdk which is obtained by User
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true);
            // Provide 0 if GDPR is not applicable and 1 if applicable
            consentObject.put("gdpr", "0");
            // Provide user consent in IAB format
            //consentObject.put(InMobiSdk.IM_GDPR_CONSENT_IAB, “<<consent in IAB format>>”);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        final View scanButtonView = findViewById(R.id.scanButton);
        final Button translateButton = findViewById(R.id.language);
        setSupportActionBar(toolbar);

        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);
        final InMobiBanner bannerAd = findViewById(R.id.bannermain);
        bannerAd.load();

        scanButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.getContext().startActivity(new Intent(mActivity, AppListActivity.class));
            }
        });

        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Locale.getDefault().getLanguage().equals("hi")) {
                    restartInLocale(new Locale("en"));
                } else {
                    restartInLocale(new Locale("hi"));
                }
            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic("india")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            //Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_SHORT).show();
                        } else {
                            //Toast.makeText(MainActivity.this, "passed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void restartInLocale(Locale locale)
    {
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        Resources resources = getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        recreate();
    }

}
