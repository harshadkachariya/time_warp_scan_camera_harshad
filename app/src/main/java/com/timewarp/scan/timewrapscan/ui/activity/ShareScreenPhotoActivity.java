package com.timewarp.scan.timewrapscan.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.databinding.ActivityShareScreenPhotoBinding;
import com.timewarp.scan.timewrapscan.ui.defaults.MainActivity;

import java.io.File;

import think.outside.the.box.handler.APIManager;
import think.outside.the.box.ui.BaseActivity;

public class ShareScreenPhotoActivity extends BaseActivity {

    private ActivityShareScreenPhotoBinding binding;
    Uri audioPath;
    int ads_const;
    SharedPreferences spref;

    public Bundle getNonPersonalizedAdsBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("npa", "1");
        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightTheme(true);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_share_screen_photo);

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
        SharedPreferences sharedPreferences = getSharedPreferences("pref_ads", 0);
        spref = sharedPreferences;
        ads_const = sharedPreferences.getInt("ads_const", 0);

        APIManager.showBanner(binding.adView);

        initView();
    }

    private void initView() {
        Intent intent = getIntent();

        if (intent.hasExtra("sharePath")) {
            audioPath = Uri.fromFile(new File(intent.getStringExtra("sharePath")));

            Glide.with((FragmentActivity) this).load(audioPath).into(binding.ivPreview);
        }
        binding.buttonBack.setOnClickListener(view -> onBackPressed());

        binding.buttonHome.setOnClickListener(view -> {
            APIManager.showInter(ShareScreenPhotoActivity.this, false, isfail -> {
                Intent intent2 = new Intent(ShareScreenPhotoActivity.this.getApplicationContext(), MainActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ShareScreenPhotoActivity.this.startActivity(intent2);
            });
        });

        findViewById(R.id.btndelete).setOnClickListener(v -> {
            File file = new File(audioPath.getPath());
            boolean deleted = file.delete();
            finish();
        });

        binding.btnShareMore.setOnClickListener(view -> {
            Intent intent2 = new Intent("android.intent.action.SEND");
            intent2.setType("*/*");
            intent2.putExtra("android.intent.extra.STREAM", ShareScreenPhotoActivity.this.audioPath);
            ShareScreenPhotoActivity.this.startActivity(Intent.createChooser(intent2, "Share Audio"));
        });
    }

    @Override
    public void onBackPressed() {
        APIManager.showInter(ShareScreenPhotoActivity.this, true, isfail -> {
            finish();
        });
    }
}