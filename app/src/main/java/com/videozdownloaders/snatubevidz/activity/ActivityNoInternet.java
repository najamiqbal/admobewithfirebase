package com.videozdownloaders.snatubevidz.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.videozdownloaders.snatubevidz.R;

public class ActivityNoInternet extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);
    }
    public void noInternet(View view) {
        finishAffinity();
    }

}