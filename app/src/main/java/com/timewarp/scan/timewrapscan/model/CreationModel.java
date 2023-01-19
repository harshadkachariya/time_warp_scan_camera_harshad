package com.timewarp.scan.timewrapscan.model;

public class CreationModel {
    String createdDateTime;
    String fileName;
    String filePath;
    long fileSize;
    String videoDuration;

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String str) {
        this.filePath = str;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String str) {
        this.fileName = str;
    }

    public String getCreatedDateTime() {
        return this.createdDateTime;
    }

    public void setCreatedDateTime(String str) {
        this.createdDateTime = str;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long j) {
        this.fileSize = j;
    }

    public String getVideoDuration() {
        return this.videoDuration;
    }

    public void setVideoDuration(String str) {
        this.videoDuration = str;
    }
}
