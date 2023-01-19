package com.timewarp.scan.timewrapscan.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Folder {
    private final String mImage;
    private final String mName;
    private final String mPath;
    private final String mPathFull;

    public Folder(String str, String str2, String str3, String str4) {
        this.mImage = str;
        this.mPath = str2;
        this.mPathFull = str3;
        this.mName = str4;
    }

    public static ArrayList<Folder> createFolderList(List<Map<String, String>> list) {
        ArrayList<Folder> arrayList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            arrayList.add(new Folder(list.get(i).get("url"), list.get(i).get("path"), list.get(i).get("fullpath"), list.get(i).get("name")));
        }
        return arrayList;
    }

    public String getImage() {
        return this.mImage;
    }

    public String getPath() {
        return this.mPath;
    }

    public String getName() {
        return this.mName;
    }

    public String getmPathFull() {
        return this.mPathFull;
    }
}
