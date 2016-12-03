package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.common.Callback.CancelledException;
import org.xutils.http.RequestParams;

import com.example.download.DownloadCallback;
import com.example.download.DownloadViewHolderDefault;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBase;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	private DataBase db;
	static MainActivity mainActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		db = LiteOrm.newSingleInstance(this, "download");
		mainActivity = this;
		
		findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onTest1Click();
			}
		});
		findViewById(R.id.button2).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onTest2Click();
			}
		});
		findViewById(R.id.button3).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onTest3Click();
			}
		});
		
		findViewById(R.id.button4).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onTest4Click();
			}
		});
		
		findViewById(R.id.button5).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onTest5Click();
			}
		});
		
		findViewById(R.id.button6).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onTest6Click();
			}
		});
	}
	
	public DataBase getDataBase(){
		return db;
	}
	
	public static MainActivity getInstance(){
		return mainActivity;
	}
	
	// 添加到下载列表
    private void onTest1Click()   {
        for (int i = 0; i < 3; i++) {
            String url = "http://ocnzhhlfe.bkt.clouddn.com/%E6%B7%B1%E5%8D%97%E4%B8%AD%E8%B7%AF.jpg";
            String label = i + "xUtils_" + System.nanoTime();
            DownloadService.getDownloadManager().startDownload(url, label,"/sdcard/xUtils/" + label + ".mp4", true, false, null);
        }
    }
    
    private void onTest2Click(){
    	String url = "http://ocnzhhlfe.bkt.clouddn.com/%E6%B7%B1%E5%8D%97%E4%B8%AD%E8%B7%AF.jpg";
    	String label = 4 + "xUtils_" + System.nanoTime();
    	
    	RequestParams params = new RequestParams(url);
		params.setAutoResume(true);
		params.setAutoRename(false);
		params.setSaveFilePath("/sdcard/xUtils/" + label + ".mp4");
		//params.setExecutor(executor);
		params.setCancelFast(true);
		
		Callback.Cancelable cancelable = x.http().get(params, new Callback.ProgressCallback<File>() {

			@Override
			public void onCancelled(CancelledException arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFinished() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(File arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onLoading(long arg0, long arg1, boolean arg2) {
				// TODO Auto-generated method stub
				Log.e("loading", arg1+"");			
			}

			@Override
			public void onStarted() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onWaiting() {
				// TODO Auto-generated method stub
				
			}

			
		});
    	
    }
	
	private void onTest3Click(){
        startActivity(new Intent(this, DownloadActivity.class));
    }
	
	
	
	
	private void onTest4Click()   {
		for (int i = 0; i < 3; i++) {
            String url = "http://api.nx87.cn/up";
            String label = i + "xUtils_" + System.nanoTime();
            UpLoadService.getDownloadManager().startDownload(url, label,"/sdcard/xUtils/" + label + ".mp4");
        }
	}
	
	
	private void onTest5Click() {
		RequestParams params = new RequestParams("http://api.nx87.cn/up");
        // 加到url里的参数, http://xxxx/s?wd=xUtils
        //params.addQueryStringParameter("wd", "xUtils");
        // 添加到请求body体的参数, 只有POST, PUT, PATCH, DELETE请求支持.
        // params.addBodyParameter("wd", "xUtils");

        // 使用multipart表单上传文件
        params.setMultipart(true);
        params.addBodyParameter( "file",  new File("/storage/emulated/0/IMG20160115094916.jpg")); // 如果文件没有扩展名, 最好设置contentType参数.
//        try {
//            params.addBodyParameter( 
//                    "file2",
//                    new FileInputStream(new File("/sdcard/test2.jpg")),
//                    "image/jpeg",
//                    // 测试中文文件名
//                    "你+& \" 好.jpg"); // InputStream参数获取不到文件名, 最好设置, 除非服务端不关心这个参数.
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//        }
        x.http().post(params, new Callback.ProgressCallback<File>() {

			@Override
			public void onCancelled(CancelledException arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFinished() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(File arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onLoading(long arg0, long arg1, boolean arg2) {
				// TODO Auto-generated method stub
				Log.e("arg1", arg1+"===");
			}

			@Override
			public void onStarted() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onWaiting() {
				// TODO Auto-generated method stub
				
			}
		});
	}

	private void onTest6Click() {
		startActivity(new Intent(this, UpLoadActivity.class));
	}

}
