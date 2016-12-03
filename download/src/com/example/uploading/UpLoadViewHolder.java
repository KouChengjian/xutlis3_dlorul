package com.example.uploading;

import java.io.File;

import org.xutils.common.Callback.CacheCallback;

import android.view.View;

public abstract class UpLoadViewHolder {

    protected UpLoad upLoad;

    public UpLoadViewHolder(View view, UpLoad upLoad) {
        this.upLoad = upLoad;
    }

    public final UpLoad getDownloadInfo() {
        return upLoad;
    }

    public void update(UpLoad upLoad) {
        this.upLoad = upLoad;
    }

    public abstract void onWaiting();

    public abstract void onStarted();

    public abstract void onLoading(long total, long current);

    public abstract void onSuccess(File result);

    public abstract void onError(Throwable ex, boolean isOnCallback);

    public abstract void onCancelled(CacheCallback.CancelledException cex);
}