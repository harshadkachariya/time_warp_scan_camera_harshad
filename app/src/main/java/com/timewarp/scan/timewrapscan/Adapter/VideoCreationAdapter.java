package com.timewarp.scan.timewrapscan.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.interfaces.OnItemClickListener;
import com.timewarp.scan.timewrapscan.model.CreationModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoCreationAdapter extends RecyclerView.Adapter<VideoCreationAdapter.ToDoViewHolder> {

    public List<CreationModel> getList() {
        return list;
    }

    private List<CreationModel> list;
    public Context context;
    OnItemClickListener onItemClickListener;

    public VideoCreationAdapter(Context context, List<CreationModel> list, OnItemClickListener onItemClickListener) {
        this.list = list;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ToDoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ToDoViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_video_creation, (ViewGroup) null, false));
    }

    public void onBindViewHolder(ToDoViewHolder toDoViewHolder, @SuppressLint("RecyclerView") final int i) {
        final CreationModel creationModel = this.list.get(i);

        TextView textView = toDoViewHolder.tvDuration;

        textView.setText("" + creationModel.getVideoDuration());

        Glide.with(this.context)
                .load(creationModel.getFilePath())
                .into(toDoViewHolder.ivVideoItem);

        toDoViewHolder.ivDelete.setOnClickListener(view -> {
            File file = new File(creationModel.getFilePath());
            if (file.exists()) {
                file.delete();
                VideoCreationAdapter.this.list.remove(i);
                VideoCreationAdapter.this.notifyDataSetChanged();
                onItemClickListener.onDelete(i);
                Log.e("TAG", "onBindViewHolder: " + getList().size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }

    public void setData(ArrayList<CreationModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public class ToDoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RelativeLayout cardItem;
        ImageView ivDelete;
        ImageView ivVideoItem;
        TextView tvDuration;

        public ToDoViewHolder(View view) {
            super(view);
            ivVideoItem = (ImageView) view.findViewById(R.id.ivVideoItem);
            tvDuration = (TextView) view.findViewById(R.id.tvDuration);
            ivDelete = (ImageView) view.findViewById(R.id.ivDelete);
            RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.cardItem);
            cardItem = relativeLayout;
            relativeLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.OnClick(view, getLayoutPosition());
        }
    }
}
