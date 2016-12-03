package com.example.uploading;

import java.io.File;
import java.lang.ref.WeakReference;

import org.xutils.common.Callback;


public class UpLoadCallback implements Callback.ProgressCallback<File> ,Callback.Cancelable {

	
	private UpLoad upLoad;
    private WeakReference<UpLoadViewHolder> viewHolderRef;
    private UpLoadManager downloadManager;
    private boolean cancelled = false;
    private Cancelable cancelable;

    public UpLoadCallback(UpLoadViewHolder viewHolder) {
        this.switchViewHolder(viewHolder);
    }
    
    public boolean switchViewHolder(UpLoadViewHolder viewHolder) {
        if (viewHolder == null) return false;

        synchronized (UpLoadCallback.class) {
            if (upLoad != null) {
                if (this.isStopped()) {
                    return false;
                }
            }
            this.upLoad = viewHolder.getDownloadInfo();
            this.viewHolderRef = new WeakReference<UpLoadViewHolder>(viewHolder);
        }
        return true;
    }
	
    private boolean isStopped() {
    	UpLoadState state = upLoad.getState();
        return isCancelled() || state.value() > UpLoadState.STARTED.value();
    }
    
    public void setDownloadManager(UpLoadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    public void setCancelable(Cancelable cancelable) {
        this.cancelable = cancelable;
    }

    private UpLoadViewHolder getViewHolder() {
        if (viewHolderRef == null) return null;
        UpLoadViewHolder viewHolder = viewHolderRef.get();
        if (viewHolder != null) {
        	UpLoad downloadInfo = viewHolder.getDownloadInfo();
            if (this.upLoad != null && this.upLoad.equals(downloadInfo)) {
                return viewHolder;
            }
        }
        return null;
    }
    
    @Override
    public void onWaiting() {
        try {
        	upLoad.setState(UpLoadState.WAITING);
            downloadManager.updateDownloadInfo(upLoad);
        } catch (Exception ex) {
            //Log.e(ex.getMessage().toString(), ex.toString());
        }
        UpLoadViewHolder viewHolder = this.getViewHolder();
        if (viewHolder != null) {
            viewHolder.onWaiting();
        }
    }

    @Override
    public void onStarted() {
        try {
        	upLoad.setState(UpLoadState.STARTED);
            downloadManager.updateDownloadInfo(upLoad);
        } catch (Exception ex) {
            //Log.e(ex.getMessage(), ex);
        }
        UpLoadViewHolder viewHolder = this.getViewHolder();
        if (viewHolder != null) {
            viewHolder.onStarted();
        }
    }

    @Override
    public void onLoading(long total, long current, boolean isDownloading) {
        if (isDownloading) {
            try {
            	upLoad.setState(UpLoadState.STARTED);
            	upLoad.setFileLength(total);
            	upLoad.setProgress((int) (current * 100 / total));
                downloadManager.updateDownloadInfo(upLoad);
            } catch (Exception ex) {
                //Log.e(ex.getMessage(), ex);
            }
            UpLoadViewHolder viewHolder = this.getViewHolder();
            if (viewHolder != null) {
                viewHolder.onLoading(total, current);
            }
        }
    }

    @Override
    public void onSuccess(File result) {
        synchronized (UpLoadCallback.class) {
            try {
            	upLoad.setState(UpLoadState.FINISHED);
                downloadManager.updateDownloadInfo(upLoad);
            } catch (Exception ex) {
                //Log.e(ex.getMessage(), ex);
            }
            UpLoadViewHolder viewHolder = this.getViewHolder();
            if (viewHolder != null) {
                viewHolder.onSuccess(result);
            }
        }
    }

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {
        synchronized (UpLoadCallback.class) {
            try {
            	upLoad.setState(UpLoadState.ERROR);
                downloadManager.updateDownloadInfo(upLoad);
            } catch (Exception e) {
                //Log.e(e.getMessage(), e);
            }
            UpLoadViewHolder viewHolder = this.getViewHolder();
            if (viewHolder != null) {
                viewHolder.onError(ex, isOnCallback);
            }
        }
    }

    @Override
    public void onCancelled(CancelledException cex) {
        synchronized (UpLoadCallback.class) {
            try {
            	upLoad.setState(UpLoadState.STOPPED);
                downloadManager.updateDownloadInfo(upLoad);
            } catch (Exception ex) {
                //Log.e(ex.getMessage(), ex);
            }
            UpLoadViewHolder viewHolder = this.getViewHolder();
            if (viewHolder != null) {
                viewHolder.onCancelled(cex);
            }
        }
    }

    @Override
    public void onFinished() {
        cancelled = false;
    }
	
    @Override
    public void cancel() {
        cancelled = true;
        if (cancelable != null) {
            cancelable.cancel();
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
