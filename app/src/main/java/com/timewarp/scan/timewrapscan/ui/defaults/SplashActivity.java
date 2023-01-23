package com.timewarp.scan.timewrapscan.ui.defaults;

import android.content.Intent;
import android.os.Bundle;
import androidx.databinding.DataBindingUtil;
import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.databinding.ActivitySplashBinding;

import aani.brothers.noty.Aanibrothers;
import think.outside.the.box.callback.AdsCallback;
import think.outside.the.box.callback.SplashCallback;
import think.outside.the.box.handler.APIManager;
import think.outside.the.box.ui.BaseActivity;


public class SplashActivity extends BaseActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightTheme(true);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        Aanibrothers.subscribeNotification(this);
        Aanibrothers.configureNotification(this,getResources().getString(R.string.app_name),R.drawable.ic_vector,R.color.transperent);

        APIManager.initializeSplash(this, new SplashCallback() {
            @Override
            public void onSuccess() {
                APIManager.showSplashAD(SplashActivity.this, new AdsCallback() {
                    @Override
                    public void onClose(boolean b) {
                        if (APIManager.hasStartScreen()) {
                            Intent intent = new Intent(SplashActivity.this, StartActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

}