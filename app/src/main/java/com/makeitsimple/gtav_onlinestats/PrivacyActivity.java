package com.makeitsimple.gtav_onlinestats;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

public class PrivacyActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrivacyActivity.this.setTitle("PRIVACY POLICY");
        setContentView(R.layout.privacy_layout);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout(   (int)(width*0.8f),(int)(height*0.6f));

    }

    public void onPolicyFinish(View view) {
        setResult(Activity.RESULT_OK);
        finish();
    }
}
