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
import com.timewarp.scan.timewrapscan.Adapter.VideoCreationAdapter;
import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.interfaces.OnItemClickListener;
import com.timewarp.scan.timewrapscan.model.CreationModel;
import com.timewarp.scan.timewrapscan.ui.activity.ShareScreenVideoActivity;
import com.timewarp.scan.timewrapscan.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;
import think.outside.the.box.handler.APIManager;

public class CreationVideoFragment extends Fragment implements OnItemClickListener {

    ArrayList<CreationModel> creationModels = new ArrayList<>();
    ProgressBar recentProgress;
    RecyclerView rvCreationList;
    LinearLayout tvNoData;
    VideoCreationAdapter videoCreationAdapter;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {

        ViewGroup viewGroup2 = (ViewGroup) layoutInflater.inflate(R.layout.fragment_creation_video, viewGroup, false);

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
            Intent intent = new Intent(getActivity(), ShareScreenVideoActivity.class);
            intent.putExtra("shareVideoPath", this.creationModels.get(i).getFilePath());
            startActivity(intent);
        });
    }

    @Override
    public void onDelete(int position) {
        if (videoCreationAdapter.getList().size() == 0) {
            this.tvNoData.setVisibility(View.VISIBLE);
            Log.e("TAG", "position: " + videoCreationAdapter.getList().size());
        } else {
            this.tvNoData.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.creationModels.clear();
        getCreation();
    }

    public void getCreation() {
        this.recentProgress.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(() -> CreationVideoFragment.this.m1667x94fc71a5());
    }

    public void m1667x94fc71a5() {
        File[] listFiles;
        try {
            for (File file : new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/TimeWarpFaceFilter/Video").listFiles()) {
                if (file.getName().toLowerCase().contains(".mp4") && file.length() > 0 && Utils.validateVideo(getContext(), file)) {
                    CreationModel creationModel = new CreationModel();
                    creationModel.setFileName(file.getName());
                    creationModel.setFilePath(file.getPath());
                    creationModel.setVideoDuration(Utils.getDurations(file));
                    creationModel.setFileSize(file.length());
                    this.creationModels.add(creationModel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getActivity().runOnUiThread(() -> m1666xbe0206());
    }


    public void m1666xbe0206() {
        Log.e("Done", "Done");

        recentProgress.setVisibility(View.GONE);
        Collections.reverse(creationModels);
        videoCreationAdapter = new VideoCreationAdapter(getActivity(), creationModels, this);
        if (creationModels.size() == 0) {
            tvNoData.setVisibility(View.VISIBLE);
        } else {
            tvNoData.setVisibility(View.GONE);
        }
        rvCreationList.setAdapter(videoCreationAdapter);

    }
}
