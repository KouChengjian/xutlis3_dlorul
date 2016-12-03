package com.example;

import java.util.List;

import com.example.download.DownloadManager;
import com.example.uploading.UpLoadManager;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class UpLoadService extends Service {

	private static UpLoadManager DOWNLOAD_MANAGER;

    public synchronized static UpLoadManager getDownloadManager() {
        if (!DownloadService.isServiceRunning(MainActivity.getInstance())) {
            Intent downloadSvr = new Intent(MainActivity.getInstance(), DownloadService.class);
            MainActivity.getInstance().startService(downloadSvr);
        }
        if (UpLoadService.DOWNLOAD_MANAGER == null) {
        	UpLoadService.DOWNLOAD_MANAGER = UpLoadManager.getInstance();
        }
        return DOWNLOAD_MANAGER;
    }

    public UpLoadService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (DOWNLOAD_MANAGER != null) {
            DOWNLOAD_MANAGER.stopAllDownload();
        }
        super.onDestroy();
    }

    public static boolean isServiceRunning(Context context) {
        boolean isRunning = false;

        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(Integer.MAX_VALUE);

        if (serviceList == null || serviceList.size() == 0) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(DownloadService.class.getName())) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
}
