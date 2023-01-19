package com.timewarp.scan.timewrapscan.interfaces;

import android.view.View;

public interface OnItemClickListener {
    void OnClick(View view, int i);

    void onDelete(int position);
}
