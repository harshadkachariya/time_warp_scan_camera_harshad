package com.timewarp.scan.timewrapscan.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.timewarp.scan.timewrapscan.Adapter.PhotoCreationAdapter;
import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.interfaces.OnItemClickListener;
import com.timewarp.scan.timewrapscan.model.CreationModel;
import com.timewarp.scan.timewrapscan.ui.activity.ShareScreenPhotoActivity;
import com.timewarp.scan.timewrapscan.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;

import think.outside.the.box.handler.APIManager;

public class CreationPhotoFragment extends Fragment implements OnItemClickListener {

    ArrayList<CreationModel> creationModels = new ArrayList<>();
    PhotoCreationAdapter photoCreationAdapter;
    ProgressBar recentProgress;
    RecyclerView rvCreationList;
    LinearLayout tvNoData;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ViewGroup viewGroup2 = (ViewGroup) layoutInflater.inflate(R.layout.fragment_creation_photo, viewGroup, false);
        this.tvNoData = viewGroup2.findViewById(R.id.tvNoData);
        this.recentProgress = (ProgressBar) viewGroup2.findViewById(R.id.recentProgress);
        RecyclerView recyclerView = (RecyclerView) viewGroup2.findViewById(R.id.rvCreationList);
        this.rvCreationList = recyclerView;
        this.rvCreationList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        return viewGroup2;

    }

    @Override
    public void OnClick(View view, int i) {
        APIManager.showInter(getActivity(), false, isfail -> {
            Intent intent = new Intent(getActivity(), ShareScreenPhotoActivity.class);
            intent.putExtra("sharePath", this.creationModels.get(i).getFilePath());
            startActivity(intent);
        });
    }

    @Override
    public void onDelete(int position) {
        if (photoCreationAdapter.getArrayList().size() == 0) {
            this.tvNoData.setVisibility(View.VISIBLE);
        } else {
            this.tvNoData.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        creationModels.clear();
        getCreation();
    }

    public void getCreation() {
        this.recentProgress.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public final void run() {
                CreationPhotoFragment.this.m1665xac59f45c();
            }
        });
    }

    public void m1665xac59f45c() {
        File[] listFiles;
        try {
            for (File file : new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/TimeWarpFaceFilter/Photo").listFiles()) {
                if (file.getName().toLowerCase().contains(".jpg") && file.length() > 0) {
                    CreationModel creationModel = new CreationModel();
                    creationModel.setFileName(file.getName());
                    creationModel.setFilePath(file.getPath());
                    creationModel.setCreatedDateTime(Utils.getCreatedDate(file.lastModified()));
                    creationModel.setFileSize(file.length());
                    this.creationModels.add(creationModel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public final void run() {
                CreationPhotoFragment.this.m1664x181b84bd();
            }
        });
    }

    public void m1664x181b84bd() {
        Log.e("Done", "Done");
        this.recentProgress.setVisibility(View.GONE);
        Collections.reverse(creationModels);
        PhotoCreationAdapter photoCreationAdapter = new PhotoCreationAdapter(getActivity(), this.creationModels, this);
        if (this.creationModels.size() == 0) {
            this.tvNoData.setVisibility(View.VISIBLE);
        } else {
            this.tvNoData.setVisibility(View.GONE);
        }
        this.photoCreationAdapter = photoCreationAdapter;
        this.rvCreationList.setAdapter(photoCreationAdapter);

    }
}
