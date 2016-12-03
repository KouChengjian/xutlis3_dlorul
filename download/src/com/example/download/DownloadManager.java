package com.example.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.http.RequestParams;

import android.util.Log;

import com.example.MainActivity;
import com.litesuits.orm.db.DataBase;
import com.litesuits.orm.db.assit.QueryBuilder;


public class DownloadManager {

	private static DownloadManager instance;

    private final static int MAX_DOWNLOAD_THREAD = 2; // 有效的值范围[1, 3], 设置为3时, 可能阻塞图片加载.
    
    private DataBase db1;
    
    private final Executor executor = new PriorityExecutor(MAX_DOWNLOAD_THREAD,true);
    
    private final List<Download> downloadInfoList = new ArrayList<Download>();
    
    private final ConcurrentHashMap<Download, DownloadCallback> callbackMap = new ConcurrentHashMap<Download, DownloadCallback>(5);
	
    private DownloadManager() {
        db1 = MainActivity.getInstance().getDataBase();
        List<Download> infoList = db1.query(Download.class);
        if (infoList != null) {
            for (Download info : infoList) {
                if (info.getState().value() < DownloadState.FINISHED.value()) {
                    info.setState(DownloadState.STOPPED);
                }
                downloadInfoList.add(info);
            }
        }   
        Log.e("downloadInfoList.size",downloadInfoList.size()+"====");
    }
    
    public static DownloadManager getInstance() {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager();
                }
            }
        }
        return instance;
    }
    
    public void updateDownloadInfo(Download info){
    	db1.update(info);
    }

    public int getDownloadListCount() {
        return downloadInfoList.size();
    }

    public Download getDownloadInfo(int index) {
        return downloadInfoList.get(index);
    }
    
    public void stopDownload(int index) {
        Download downloadInfo = downloadInfoList.get(index);
        stopDownload(downloadInfo);
    }

    public void stopDownload(Download downloadInfo) {
        Callback.Cancelable cancelable = callbackMap.get(downloadInfo);
        if (cancelable != null) {
            cancelable.cancel();
        }
    }

    public void stopAllDownload() {
        for (Download downloadInfo : downloadInfoList) {
            Callback.Cancelable cancelable = callbackMap.get(downloadInfo);
            if (cancelable != null) {
                cancelable.cancel();
            }
        }
    }

    public void removeDownload(int index) {
    	Download downloadInfo = downloadInfoList.get(index);
        db1.delete(downloadInfo);
        stopDownload(downloadInfo);
        downloadInfoList.remove(index);
    }

    public void removeDownload(Download downloadInfo) {
        db1.delete(downloadInfo);
        stopDownload(downloadInfo);
        downloadInfoList.remove(downloadInfo);
    }
    
    public synchronized void startDownload(String url, String label,String savePath) {
    	startDownload(url ,label ,savePath ,true ,false ,null);
    }
    
	public synchronized void startDownload(String url, String label,
			String savePath, boolean autoResume, boolean autoRename,
			DownloadViewHolder viewHolder) {

		String fileSavePath = new File(savePath).getAbsolutePath();
		// DownloadInfo downloadInfo = db.selector(DownloadInfo.class)
		// .where("label", "=", label)
		// .and("fileSavePath", "=", fileSavePath)
		// .findFirst();
		QueryBuilder qb = new QueryBuilder(Download.class);
		qb = new QueryBuilder(Download.class).whereEquals("label", label)
				.whereAppendAnd().whereEquals("fileSavePath", fileSavePath);
		List<Download> list = db1.<Download> query(qb);
		Download downloadInfo = null;
		if (list.size() > 0) {
			downloadInfo = list.get(0);
			if (downloadInfo != null) {
				DownloadCallback callback = callbackMap.get(downloadInfo);
				if (callback != null) {
					if (viewHolder == null) {
						viewHolder = new DownloadViewHolderDefault(null,downloadInfo);
					}
					if (callback.switchViewHolder(viewHolder)) {
						return;
					} else {
						callback.cancel();
					}
				}
			}
		}
		// create download info
		if (downloadInfo == null) {
			downloadInfo = new Download();
			downloadInfo.setUrl(url);
			downloadInfo.setAutoRename(autoRename);
			downloadInfo.setAutoResume(autoResume);
			downloadInfo.setLabel(label);
			downloadInfo.setFileSavePath(fileSavePath);
			db1.save(downloadInfo);
		}

		// start downloading
		if (viewHolder == null) {
			viewHolder = new DownloadViewHolderDefault(null, downloadInfo);
		}
		DownloadCallback callback = new DownloadCallback(viewHolder);
		callback.setDownloadManager(this);
		callback.switchViewHolder(viewHolder);
		
		RequestParams params = new RequestParams(url);
		params.setAutoResume(downloadInfo.isAutoResume());
		params.setAutoRename(downloadInfo.isAutoRename());
		params.setSaveFilePath(downloadInfo.getFileSavePath());
		params.setExecutor(executor);
		params.setCancelFast(true);
		
		Callback.Cancelable cancelable = x.http().get(params, callback);
		callback.setCancelable(cancelable);
		callbackMap.put(downloadInfo, callback);

		if (!downloadInfoList.contains(downloadInfo)) {
			downloadInfoList.add(downloadInfo);
		}
	}
    
    
}
