package com.timewarp.scan.timewrapscan.ui.screen;

import android.content.Intent;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.databinding.ActivityScreen3Binding;
import com.timewarp.scan.timewrapscan.ui.defaults.MainActivity;

import think.outside.the.box.handler.APIManager;
import think.outside.the.box.ui.BaseActivity;

public class Screen3Activity extends BaseActivity {

    private ActivityScreen3Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightTheme(true);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_screen3);

        APIManager.showNative(binding.nativeAdd);

        binding.btnNext.setOnClickListener(view -> {
            APIManager.showInter(Screen3Activity.this, false, isfail -> {
                if (APIManager.getStartScreenCount()==4){
                    startActivity(new Intent(Screen3Activity.this, MainActivity.class));
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                else {
                    startActivity(new Intent(Screen3Activity.this, Screen4Activity.class));
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });
        });
    }

    @Override
    public void onBackPressed() {
        APIManager.showInter(Screen3Activity.this, true, isfail -> {
            finish();
        });
    }
}