package com.timewarp.scan.timewrapscan.ui.screen;

import android.content.Intent;
import android.os.Bundle;
import androidx.databinding.DataBindingUtil;
import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.databinding.ActivityScreen5Binding;
import com.timewarp.scan.timewrapscan.ui.defaults.MainActivity;
import think.outside.the.box.handler.APIManager;
import think.outside.the.box.ui.BaseActivity;

public class Screen5Activity extends BaseActivity {

    private ActivityScreen5Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightTheme(true);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_screen5);
        binding.btnDone.setOnClickListener(view -> {
            APIManager.showInter(Screen5Activity.this, false, isfail -> {
                startActivity(new Intent(Screen5Activity.this, MainActivity.class));
            });

        });
    }

    @Override
    public void onBackPressed() {
        APIManager.showInter(Screen5Activity.this, true, isfail -> {
            finish();
        });
    }
}