package com.example.download;

import java.io.File;

import org.xutils.common.Callback.CacheCallback;

import android.view.View;

public abstract class DownloadViewHolder {

    protected Download downloadInfo;

    public DownloadViewHolder(View view, Download downloadInfo) {
        this.downloadInfo = downloadInfo;
    }

    public final Download getDownloadInfo() {
        return downloadInfo;
    }

    public void update(Download downloadInfo) {
        this.downloadInfo = downloadInfo;
    }

    public abstract void onWaiting();

    public abstract void onStarted();

    public abstract void onLoading(long total, long current);

    public abstract void onSuccess(File result);

    public abstract void onError(Throwable ex, boolean isOnCallback);

    public abstract void onCancelled(CacheCallback.CancelledException cex);
}
