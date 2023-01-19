package com.timewarp.scan.timewrapscan.ui.screen;

import android.content.Intent;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.databinding.ActivityScreen2Binding;
import com.timewarp.scan.timewrapscan.ui.defaults.MainActivity;
import com.timewarp.scan.timewrapscan.ui.defaults.StartActivity;

import think.outside.the.box.handler.APIManager;
import think.outside.the.box.ui.BaseActivity;

public class Screen2Activity extends BaseActivity {

    private ActivityScreen2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightTheme(true);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_screen2);

        APIManager.showBanner(binding.bannerAdd);
        APIManager.showNative(binding.native200);

        binding.btnNext.setOnClickListener(view -> {
            APIManager.showInter(Screen2Activity.this, false, isfail -> {
                if (APIManager.getStartScreenCount()==3){
                    startActivity(new Intent(Screen2Activity.this, MainActivity.class));
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                else {
                    startActivity(new Intent(Screen2Activity.this, Screen3Activity.class));
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });
        });
    }

    @Override
    public void onBackPressed() {
        APIManager.showInter(Screen2Activity.this, true, isfail -> {
            finish();
        });
    }
}