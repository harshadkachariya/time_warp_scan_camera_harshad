package com.timewarp.scan.timewrapscan.ui.screen;

import android.content.Intent;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.databinding.ActivityScreen1Binding;
import com.timewarp.scan.timewrapscan.ui.defaults.MainActivity;

import think.outside.the.box.handler.APIManager;
import think.outside.the.box.ui.BaseActivity;

public class Screen1Activity extends BaseActivity {

    private ActivityScreen1Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setLightTheme(true);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_screen1);

        APIManager.showNative(binding.native200);
        APIManager.showBanner(binding.bannerAdd);
        binding.btnNext.setOnClickListener(view -> {
            APIManager.showInter(Screen1Activity.this, false, isfail -> {
                if (APIManager.getStartScreenCount()==2){
                    startActivity(new Intent(Screen1Activity.this,MainActivity.class));
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
               else {
                    startActivity(new Intent(Screen1Activity.this, Screen2Activity.class));
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });

        });
    }

    @Override
    public void onBackPressed() {
        APIManager.showInter(Screen1Activity.this, true, isfail -> {
            finish();
        });
    }
}