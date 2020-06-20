package com.falcon.switchapp;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

public class NewsActivity extends AppCompatActivity {

    private static final String SHARED_PREF_KEY = "NEWS_DATA";
    private static final String NEWS_KEY_PREFIX = "NEWS_KEY_";
    private static final String NEWS_TITLE_KEY_SUFFIX = "_TITLE";
    private static final String NEWS_IMAGE_KEY_SUFFIX = "_IMAGE";
    private static final String NEWS_CONTENT_KEY_SUFFIX = "_CONTENT";
    private static final String NEWS_COUNT_KEY = "NEWS_COUNT_KEY";
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
