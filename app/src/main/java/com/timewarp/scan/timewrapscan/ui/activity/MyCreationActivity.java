package com.timewarp.scan.timewrapscan.ui.activity;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;


import com.timewarp.scan.timewrapscan.Adapter.MyTabPageAdapter;
import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.databinding.ActivityMyCreationBinding;
import com.timewarp.scan.timewrapscan.ui.fragment.CreationPhotoFragment;
import com.timewarp.scan.timewrapscan.ui.fragment.CreationVideoFragment;

import think.outside.the.box.handler.APIManager;
import think.outside.the.box.ui.BaseActivity;

public class MyCreationActivity extends BaseActivity {

    private ActivityMyCreationBinding binding;
    private MyTabPageAdapter pageAdapter;
    SharedPreferences spref;
    int ads_const;

    public Bundle getNonPersonalizedAdsBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("npa", "1");
        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightTheme(true);

        binding=ActivityMyCreationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        SharedPreferences sharedPreferences = getSharedPreferences("pref_ads", 0);
        spref = sharedPreferences;
        ads_const = sharedPreferences.getInt("ads_const", 0);

        APIManager.showBanner(binding.adView);

        initView();
    }

    private void initView() {
        binding.llPhoto.setOnClickListener(v -> binding.viewPager.setCurrentItem(0));
        binding.llVideo.setOnClickListener(v -> binding.viewPager.setCurrentItem(1));

        MyTabPageAdapter myTabPageAdapter = new MyTabPageAdapter(getSupportFragmentManager());
        pageAdapter = myTabPageAdapter;
        myTabPageAdapter.addFragments(new CreationPhotoFragment(), "PHOTO");
        pageAdapter.addFragments(new CreationVideoFragment(), "VIDEO");
        binding.viewPager.setAdapter(pageAdapter);

        binding.viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    binding.llPhoto.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MyCreationActivity.this, R.color.blackColor)));
                    binding.llVideo.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MyCreationActivity.this, R.color.white)));
                    binding.tvPhoto.setTextColor(ContextCompat.getColor(MyCreationActivity.this, R.color.white));
                    binding.tvVideo.setTextColor(ContextCompat.getColor(MyCreationActivity.this, R.color.blackColor));
                    binding.ivImage.setImageResource(R.drawable.ic_imgage_black);
                    binding.ivVideo.setImageResource(R.drawable.ic_video_yellow);
                } else {
                    binding.ivImage.setImageResource(R.drawable.ic_imgage_yellow);
                    binding.ivVideo.setImageResource(R.drawable.ic_video_black);
                    binding.llPhoto.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MyCreationActivity.this, R.color.white)));
                    binding.llVideo.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MyCreationActivity.this, R.color.blackColor)));
                    binding.tvPhoto.setTextColor(ContextCompat.getColor(MyCreationActivity.this, R.color.blackColor));
                    binding.tvVideo.setTextColor(ContextCompat.getColor(MyCreationActivity.this, R.color.white));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case 16908332:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onBackPressed() {
        APIManager.showInter(MyCreationActivity.this, true, isfail -> {
            finish();
        });
    }
}