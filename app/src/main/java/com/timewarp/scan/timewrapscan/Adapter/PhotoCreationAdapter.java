package com.timewarp.scan.timewrapscan.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.interfaces.OnItemClickListener;
import com.timewarp.scan.timewrapscan.model.CreationModel;
import com.timewarp.scan.timewrapscan.utils.Utils;

import java.io.File;
import java.util.ArrayList;

public class PhotoCreationAdapter extends RecyclerView.Adapter<PhotoCreationAdapter.ItemViewHolder> {

    public ArrayList<CreationModel> getArrayList() {
        return arrayList;
    }

    private ArrayList<CreationModel> arrayList;
    private Context context;
    OnItemClickListener itemClickListener;

    public PhotoCreationAdapter(Context context, ArrayList<CreationModel> arrayList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.arrayList = arrayList;
        this.itemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_photo_creation, viewGroup, false));
    }

    public void onBindViewHolder(ItemViewHolder itemViewHolder, @SuppressLint("RecyclerView") final int i) {

        Glide.with(this.context)
                .load(this.arrayList.get(i).getFilePath())
                .into(itemViewHolder.ivImageItem);

        TextView textView = itemViewHolder.tvFileSize;

        textView.setText(" " + Utils.getStorageSize(this.arrayList.get(i).getFileSize()));

        itemViewHolder.ivDelete.setOnClickListener(view -> {
            try {
                File file = new File(((CreationModel) PhotoCreationAdapter.this.arrayList.get(i)).getFilePath());
                if (file.exists()) {
                    file.delete();
                    PhotoCreationAdapter.this.arrayList.remove(i);
                    PhotoCreationAdapter.this.notifyDataSetChanged();
                    itemClickListener.onDelete(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public void setData(ArrayList<CreationModel> list){
        this.arrayList = list;
        notifyDataSetChanged();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardItem;
        ImageView ivDelete;
        ImageView ivImageItem;
        TextView tvFileSize;

        public ItemViewHolder(View view) {
            super(view);
            this.cardItem = (CardView) view.findViewById(R.id.cardItem);
            this.ivImageItem = (ImageView) view.findViewById(R.id.ivImageItem);
            this.ivDelete = (ImageView) view.findViewById(R.id.ivDelete);
            this.tvFileSize = (TextView) view.findViewById(R.id.tvSize);

            this.cardItem.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            PhotoCreationAdapter.this.itemClickListener.OnClick(view, getLayoutPosition());
        }
    }
}
