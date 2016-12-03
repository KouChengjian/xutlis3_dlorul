package com.example.uploading;

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

public class UpLoadManager {

	
	private static UpLoadManager instance;

    private final static int MAX_DOWNLOAD_THREAD = 2; // 有效的值范围[1, 3], 设置为3时, 可能阻塞图片加载.
    
    private DataBase db;
    
    private final Executor executor = new PriorityExecutor(MAX_DOWNLOAD_THREAD,true);
    
    private final List<UpLoad> upLoadList = new ArrayList<UpLoad>();
    
    private final ConcurrentHashMap<UpLoad, UpLoadCallback> callbackMap = new ConcurrentHashMap<UpLoad, UpLoadCallback>(5);
	
    
    private UpLoadManager() {
        db = MainActivity.getInstance().getDataBase();
        List<UpLoad> infoList = db.query(UpLoad.class);
        if (infoList != null) {
            for (UpLoad info : infoList) {
                if (info.getState().value() < UpLoadState.FINISHED.value()) {
                    info.setState(UpLoadState.STOPPED);
                }
                upLoadList.add(info);
            }
        }   
        Log.e("downloadInfoList.size",upLoadList.size()+"====");
    }
    
    public static UpLoadManager getInstance() {
        if (instance == null) {
            synchronized (UpLoadManager.class) {
                if (instance == null) {
                    instance = new UpLoadManager();
                }
            }
        }
        return instance;
    }
    
    public void updateDownloadInfo(UpLoad info){
    	db.update(info);
    }

    public int getDownloadListCount() {
        return upLoadList.size();
    }

    public UpLoad getDownloadInfo(int index) {
        return upLoadList.get(index);
    }
    
    public void stopDownload(int index) {
    	UpLoad downloadInfo = upLoadList.get(index);
        stopDownload(downloadInfo);
    }

    public void stopDownload(UpLoad downloadInfo) {
        Callback.Cancelable cancelable = callbackMap.get(downloadInfo);
        if (cancelable != null) {
            cancelable.cancel();
        }
    }

    public void stopAllDownload() {
        for (UpLoad downloadInfo : upLoadList) {
            Callback.Cancelable cancelable = callbackMap.get(downloadInfo);
            if (cancelable != null) {
                cancelable.cancel();
            }
        }
    }

    public void removeDownload(int index) {
    	UpLoad downloadInfo = upLoadList.get(index);
        db.delete(downloadInfo);
        stopDownload(downloadInfo);
        upLoadList.remove(index);
    }

    public void removeDownload(UpLoad downloadInfo) {
        db.delete(downloadInfo);
        stopDownload(downloadInfo);
        upLoadList.remove(downloadInfo);
    }
    
    public synchronized void startDownload(String url, String label,String savePath) {
    	startDownload(url ,label ,savePath ,true ,false ,null);
    }
    
	public synchronized void startDownload(String url, String label,
			String savePath, boolean autoResume, boolean autoRename,
			UpLoadViewHolder viewHolder) {
		String fileSavePath = new File(savePath).getAbsolutePath();
		// DownloadInfo downloadInfo = db.selector(DownloadInfo.class)
		// .where("label", "=", label)
		// .and("fileSavePath", "=", fileSavePath)
		// .findFirst();
		QueryBuilder qb = new QueryBuilder(UpLoad.class);
		
		qb = new QueryBuilder(UpLoad.class).whereEquals("label", label)
				.whereAppendAnd().whereEquals("fileSavePath", fileSavePath);
		
		List<UpLoad> list = db.<UpLoad> query(qb);
		UpLoad downloadInfo = null;
		if (list.size() > 0) {
			downloadInfo = list.get(0);
			if (downloadInfo != null) {
				UpLoadCallback callback = callbackMap.get(downloadInfo);
				if (callback != null) {
					if (viewHolder == null) {
						viewHolder = new UpLoadViewHolderDefault(null,downloadInfo);
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
			downloadInfo = new UpLoad();
			downloadInfo.setUrl(url);
			downloadInfo.setAutoRename(autoRename);
			downloadInfo.setAutoResume(autoResume);
			downloadInfo.setLabel(label);
			downloadInfo.setFileSavePath(fileSavePath);
			db.save(downloadInfo);
		}
				
		// start downloading
		if (viewHolder == null) {
			viewHolder = new UpLoadViewHolderDefault(null, downloadInfo);
		}

		UpLoadCallback callback = new UpLoadCallback(viewHolder);
		callback.setDownloadManager(this);
		callback.switchViewHolder(viewHolder);
		
		RequestParams params = new RequestParams(url);
		params.addBodyParameter( "file",  new File("/storage/emulated/0/IMG20160115094916.jpg"));
		params.setAutoResume(downloadInfo.isAutoResume());
		params.setAutoRename(downloadInfo.isAutoRename());
		params.setSaveFilePath(downloadInfo.getFileSavePath());
		params.setExecutor(executor);
		params.setCancelFast(true);
		params.setMultipart(true);
		
		Callback.Cancelable cancelable = x.http().post(params, callback);
		callback.setCancelable(cancelable);
		callbackMap.put(downloadInfo, callback);

		if (!upLoadList.contains(downloadInfo)) {
			upLoadList.add(downloadInfo);
		}
	}
    
}
