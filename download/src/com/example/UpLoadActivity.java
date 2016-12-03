package com.example;

import java.io.File;

import org.xutils.common.Callback;

import com.example.uploading.UpLoad;
import com.example.uploading.UpLoadManager;
import com.example.uploading.UpLoadState;
import com.example.uploading.UpLoadViewHolder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class UpLoadActivity extends Activity{

	private ListView upLoadList;
	private UpLoadListAdapter upLoadListAdapter;
    private UpLoadManager upLoadManager;
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		
		upLoadList = (ListView)findViewById(R.id.lv_download);
		upLoadManager = UpLoadService.getDownloadManager();
		upLoadListAdapter = new UpLoadListAdapter();
		upLoadList.setAdapter(upLoadListAdapter);
	}
	
	
	private class UpLoadListAdapter extends BaseAdapter {

        private Context mContext;
        private final LayoutInflater mInflater;

        private UpLoadListAdapter() {
            mContext = getBaseContext();
            mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            if (upLoadManager == null) return 0;
            return upLoadManager.getDownloadListCount();
        }

        @Override
        public Object getItem(int i) {
            return upLoadManager.getDownloadInfo(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
        	UpLoadItemViewHolder holder = null;
            UpLoad downloadInfo = upLoadManager.getDownloadInfo(i);
            if (view == null) {
                view = mInflater.inflate(R.layout.download_item, null);
                holder = new UpLoadItemViewHolder(view, downloadInfo);
                view.setTag(holder);
                holder.refresh();
            } else {
                holder = (UpLoadItemViewHolder) view.getTag();
                holder.update(downloadInfo);
            }

            if (downloadInfo.getState().value() < UpLoadState.FINISHED.value()) {
            	upLoadManager.startDownload(
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
	
	public class UpLoadItemViewHolder extends UpLoadViewHolder implements OnClickListener{
        TextView label;
        TextView state;
        ProgressBar progressBar;
        Button stopBtn;
        Button removeBtn;

        public UpLoadItemViewHolder(View view, UpLoad upLoad) {
            super(view, upLoad);
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
        	UpLoadState state = upLoad.getState();
            switch (state) {
                case WAITING:
                case STARTED:
                	upLoadManager.stopDownload(upLoad);
                    break;
                case ERROR:
                case STOPPED:
                	upLoadManager.startDownload(
                			upLoad.getUrl(),
                			upLoad.getLabel(),
                			upLoad.getFileSavePath(),
                			upLoad.isAutoResume(),
                            upLoad.isAutoRename(),
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
        	upLoadManager.removeDownload(upLoad);
        	upLoadListAdapter.notifyDataSetChanged();
        }

        @Override
        public void update(UpLoad upLoad) {
            super.update(upLoad);
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
            label.setText(upLoad.getLabel());
            state.setText(upLoad.getState().toString());
            progressBar.setProgress(upLoad.getProgress());

            stopBtn.setVisibility(View.VISIBLE);
            stopBtn.setText(MainActivity.getInstance().getString(R.string.stop));
            UpLoadState state = upLoad.getState();
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
