package com.timewarp.scan.timewrapscan.ui.defaults;

import android.content.Intent;
import android.os.Bundle;
import androidx.databinding.DataBindingUtil;

import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.databinding.ActivityMainBinding;
import com.timewarp.scan.timewrapscan.ui.WaterFallVideo.HomeWaterFallActivity;
import com.timewarp.scan.timewrapscan.ui.activity.MyCreationActivity;
import com.timewarp.scan.timewrapscan.ui.activity.TimeWrapFaceActivity;

import think.outside.the.box.handler.APIManager;
import think.outside.the.box.ui.BaseActivity;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightTheme(true);

       binding=ActivityMainBinding.inflate(getLayoutInflater());
       setContentView(binding.getRoot());
        APIManager.showBanner(binding.adsBanner);

        binding.btnFaceFilter.setOnClickListener(view -> {
            APIManager.showInter(MainActivity.this, false, isfail -> {
                startActivity(new Intent(MainActivity.this,TimeWrapFaceActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            });
        });

        binding.btnWaterFall.setOnClickListener(view -> {
            APIManager.showInter(MainActivity.this, false, isfail -> {
                startActivity(new Intent(MainActivity.this,HomeWaterFallActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            });
        });

        binding.btnMyCreation.setOnClickListener(view -> {
            APIManager.showInter(MainActivity.this, false, isfail -> {
                startActivity(new Intent(MainActivity.this,MyCreationActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            });
        });
    }

    @Override
    public void onBackPressed() {
        APIManager.showRattingDialog(this, () -> {
            APIManager.showExitDialog(this);
        });
    }
}