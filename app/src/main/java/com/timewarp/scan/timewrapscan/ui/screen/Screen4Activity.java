package com.timewarp.scan.timewrapscan.ui.screen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.databinding.ActivityScreen4Binding;
import com.timewarp.scan.timewrapscan.ui.defaults.MainActivity;

import think.outside.the.box.handler.APIManager;
import think.outside.the.box.ui.BaseActivity;

public class Screen4Activity extends BaseActivity {

    private ActivityScreen4Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightTheme(true);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_screen4);

        APIManager.showNative(binding.nativeAdd);

        binding.btnAccept.setOnClickListener(view -> {
            if (binding.acceptTermsAndConditions.isChecked()){
                APIManager.showInter(Screen4Activity.this, false, isfail -> {
                    if (APIManager.getStartScreenCount()==5){
                        startActivity(new Intent(Screen4Activity.this, MainActivity.class));
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                    else {
                        startActivity(new Intent(Screen4Activity.this, Screen5Activity.class));
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                });
            }
            else {
                Toast.makeText(this, "Please Click Check Box", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        APIManager.showInter(Screen4Activity.this, true, isfail -> {
            finish();
        });
    }
}