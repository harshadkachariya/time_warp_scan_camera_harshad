package com.timewarp.scan.timewrapscan.ui.defaults;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.databinding.ActivityStartBinding;
import com.timewarp.scan.timewrapscan.ui.screen.Screen1Activity;
import com.timewarp.scan.timewrapscan.ui.screen.Screen2Activity;

import think.outside.the.box.handler.APIManager;
import think.outside.the.box.ui.BaseActivity;


public class StartActivity extends BaseActivity {

    private ActivityStartBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLightTheme(true);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_start);

        APIManager.showNative(binding.nativeAdd);

        if (APIManager.isUpdate()) {
            APIManager.showUpdateDialog(this);
        }

        APIManager.showPromoAdDialog(this, true);

        binding.btnStart.setOnClickListener(view -> {
            APIManager.showInter(StartActivity.this, false, isfail -> {
                if (APIManager.getStartScreenCount()==1){
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);

                }
                else {
                    startActivity(new Intent(StartActivity.this, Screen1Activity.class));
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });
        });

        binding.btnShare.setOnClickListener(view -> {
            String app = getString(R.string.app_name);
            Intent share = new Intent("android.intent.action.SEND");
            share.setType("text/plain");
            share.putExtra("android.intent.extra.TEXT", app + "\n\n" + "Open this Link on Play Store" + "\n\n" + "https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(Intent.createChooser(share, "Share Application"));
        });

        binding.btnPrivacy.setOnClickListener(view -> {
            APIManager.showPrivacyDialog(StartActivity.this, getResources().getStringArray(R.array.terms_of_service));
        });
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}