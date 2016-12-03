package com.example;

import java.io.File;

import org.xutils.common.Callback;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.download.Download;
import com.example.download.DownloadManager;
import com.example.download.DownloadState;
import com.example.download.DownloadViewHolder;

public class DownloadActivity extends Activity {

	private ListView downloadList;

    private DownloadManager downloadManager;
    private DownloadListAdapter downloadListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		
		downloadList = (ListView)findViewById(R.id.lv_download);
		downloadManager = DownloadService.getDownloadManager();
	
        downloadListAdapter = new DownloadListAdapter();
        
        downloadList.setAdapter(downloadListAdapter);
	}
	
	private class DownloadListAdapter extends BaseAdapter {

        private Context mContext;
        private final LayoutInflater mInflater;

        private DownloadListAdapter() {
            mContext = getBaseContext();
            mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            if (downloadManager == null) return 0;
            return downloadManager.getDownloadListCount();
        }

        @Override
        public Object getItem(int i) {
            return downloadManager.getDownloadInfo(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            DownloadItemViewHolder holder = null;
            Download downloadInfo = downloadManager.getDownloadInfo(i);
            if (view == null) {
                view = mInflater.inflate(R.layout.download_item, null);
                holder = new DownloadItemViewHolder(view, downloadInfo);
                view.setTag(holder);
                holder.refresh();
            } else {
                holder = (DownloadItemViewHolder) view.getTag();
                holder.update(downloadInfo);
            }

            if (downloadInfo.getState().value() < DownloadState.FINISHED.value()) {
            	downloadManager.startDownload(
                        downloadInfo.getUrl(),
                        downloadInfo.getLabel(),
                        downloadInfo.getFileSavePath(),
                        downloadInfo.isAutoResume(),
                        downloadInfo.isAutoRename(),
                        holder);
            }

            return view;
        }
    }
	
	public class DownloadItemViewHolder extends DownloadViewHolder implements OnClickListener{
        TextView label;
        TextView state;
        ProgressBar progressBar;
        Button stopBtn;
        Button removeBtn;

        public DownloadItemViewHolder(View view, Download downloadInfo) {
            super(view, downloadInfo);
            label = (TextView)view.findViewById(R.id.download_label);
            state = (TextView)view.findViewById(R.id.download_state);
            progressBar = (ProgressBar)view.findViewById(R.id.download_pb);
            stopBtn = (Button)view.findViewById(R.id.download_stop_btn);
            removeBtn = (Button)view.findViewById(R.id.download_remove_btn);
            stopBtn.setOnClickListener(this);
            removeBtn.setOnClickListener(this);
            refresh();
        }
        
        @Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.download_stop_btn:
				toggleEvent();
				break;
            case R.id.download_remove_btn:
            	removeEvent();
				break;
			default:
				break;
			}
		}

        private void toggleEvent() {
            DownloadState state = downloadInfo.getState();
            switch (state) {
                case WAITING:
                case STARTED:
                    downloadManager.stopDownload(downloadInfo);
                    break;
                case ERROR:
                case STOPPED:
                	downloadManager.startDownload(
                            downloadInfo.getUrl(),
                            downloadInfo.getLabel(),
                            downloadInfo.getFileSavePath(),
                            downloadInfo.isAutoResume(),
                            downloadInfo.isAutoRename(),
                            this);
                    break;
                case FINISHED:
                    Toast.makeText(MainActivity.getInstance(), "已经下载完成", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }

        private void removeEvent() {
        	downloadManager.removeDownload(downloadInfo);
            downloadListAdapter.notifyDataSetChanged();
        }

        @Override
        public void update(Download downloadInfo) {
            super.update(downloadInfo);
            refresh();
        }

        @Override
        public void onWaiting() {
            refresh();
        }

        @Override
        public void onStarted() {
            refresh();
        }

        @Override
        public void onLoading(long total, long current) {
            refresh();
        }

        @Override
        public void onSuccess(File result) {
            refresh();
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
        	Log.e("ex", ex.toString()+"===");
            refresh();
        }

        @Override
        public void onCancelled(Callback.CancelledException cex) {
        	Log.e("cex", cex.toString()+"===");
            refresh();
        }

        public void refresh() {
            label.setText(downloadInfo.getLabel());
            state.setText(downloadInfo.getState().toString());
            progressBar.setProgress(downloadInfo.getProgress());

            stopBtn.setVisibility(View.VISIBLE);
            stopBtn.setText(MainActivity.getInstance().getString(R.string.stop));
            DownloadState state = downloadInfo.getState();
            switch (state) {
                case WAITING:
                case STARTED:
                    stopBtn.setText(MainActivity.getInstance().getString(R.string.stop));
                    break;
                case ERROR:
                case STOPPED:
                    stopBtn.setText(MainActivity.getInstance().getString(R.string.start));
                    break;
                case FINISHED:
                    stopBtn.setVisibility(View.INVISIBLE);
                    break;
                default:
                    stopBtn.setText(MainActivity.getInstance().getString(R.string.start));
                    break;
            }
        }

    }
	
}
