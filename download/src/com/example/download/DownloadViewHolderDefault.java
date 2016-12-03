package com.example.download;

import java.io.File;

import org.xutils.common.Callback;

import android.view.View;


/**
 * Created by wyouflf on 15/11/11.
 */
public class DownloadViewHolderDefault extends DownloadViewHolder {

	public DownloadViewHolderDefault(View view, Download downloadInfo) {
		super(view, downloadInfo);
	}

	@Override
	public void onWaiting() {

	}

	@Override
	public void onStarted() {

	}

	@Override
	public void onLoading(long total, long current) {

	}

	@Override
	public void onSuccess(File result) {
		// Toast.makeText(x.app(), "下载完成", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onError(Throwable ex, boolean isOnCallback) {
		// Toast.makeText(x.app(), "下载失败", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onCancelled(Callback.CancelledException cex) {
	}
}
