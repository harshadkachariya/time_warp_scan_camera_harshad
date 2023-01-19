package com.timewarp.scan.timewrapscan.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.MediaController;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.databinding.ActivityShareScreenVideoBinding;
import com.timewarp.scan.timewrapscan.ui.defaults.MainActivity;
import java.io.File;
import think.outside.the.box.handler.APIManager;
import think.outside.the.box.ui.BaseActivity;

public class ShareScreenVideoActivity extends BaseActivity {

    private ActivityShareScreenVideoBinding binding;
    SharedPreferences spref;
    Uri videoUri;
    int ads_const;
    MediaController mediaControls;

    public Bundle getNonPersonalizedAdsBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("npa", "1");
        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightTheme(true);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_share_screen_video);

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
        SharedPreferences sharedPreferences = getSharedPreferences("pref_ads", 0);
        this.spref = sharedPreferences;
        this.ads_const = sharedPreferences.getInt("ads_const", 0);

        APIManager.showBanner(binding.adView);

        initView();
    }

    private void initView() {

        if (getIntent().hasExtra("shareVideoPath")) {
            videoUri = Uri.parse(getIntent().getStringExtra("shareVideoPath"));

            Glide.with((FragmentActivity) this)
                    .load(videoUri.toString())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.ivVideoThumb);
        }

        if (mediaControls == null) {
            MediaController mediaController = new MediaController(this);
            mediaControls = mediaController;
            mediaController.setAnchorView(binding.ivPreview);
        }
        binding.ivPreview.setMediaController(this.mediaControls);

        binding.ivPreview.setOnCompletionListener(mediaPlayer -> {
            binding.rlVideSet.setVisibility(View.VISIBLE);
            binding.ivPreview.setVisibility(View.GONE);
        });

        binding.rlVideSet.setOnClickListener(view -> {
            try {
                binding.rlVideSet.setVisibility(View.GONE);
                binding.ivPreview.setVisibility(View.VISIBLE);
                binding.ivPreview.setVideoURI(ShareScreenVideoActivity.this.videoUri);
                binding.ivPreview.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.buttonBack.setOnClickListener(view -> onBackPressed());

        binding.buttonHome.setOnClickListener(view -> {
            APIManager.showInter(ShareScreenVideoActivity.this, false, isfail -> {
                Intent intent = new Intent(ShareScreenVideoActivity.this.getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ShareScreenVideoActivity.this.startActivity(intent);
            });
        });

        binding.btnShareMore.setOnClickListener(view -> {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("*/*");
            intent.putExtra("android.intent.extra.STREAM", ShareScreenVideoActivity.this.videoUri);
            ShareScreenVideoActivity.this.startActivity(Intent.createChooser(intent, "Share Video"));
        });

        findViewById(R.id.btndelete).setOnClickListener(v -> {
            File file = new File(videoUri.getPath());
            boolean deleted = file.delete();
            finish();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoUri != null && binding.rlVideSet.getVisibility() == View.GONE) {
            binding.rlVideSet.setVisibility(View.VISIBLE);
            binding.ivPreview.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        APIManager.showInter(ShareScreenVideoActivity.this, true, isfail -> {
            finish();
        });
    }
}