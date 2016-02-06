package com.cqyw.smart.main.update;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.cqyw.smart.R;
import com.cqyw.smart.config.AppConstants;
import com.cqyw.smart.main.activity.MainActivity;

import java.io.IOException;
import java.lang.ref.WeakReference;


/**
 * Created by Kairong on 2015/11/9.
 * mail:wangkrhust@gmail.com
 */
public class DownloadServices extends Service {
    private final static int DOWNLOAD_COMPLETE = -2;
    private final static int DOWNLOAD_FAIL = -1;

    //自定义通知栏类
    MyNotification myNotification;

    String filePathString; //下载文件绝对路径(包括文件名)

    //通知栏跳转Intent
    private Intent updateIntent = null;
    private PendingIntent updatePendingIntent = null;

    DownFileThread downFileThread;  //自定义文件下载线程


    public static class MyHandlder extends Handler{
        WeakReference<DownloadServices> services;
        MyHandlder(DownloadServices services){
            this.services = new WeakReference<DownloadServices>(services);
        }
        @Override
        public void handleMessage(Message msg) {
            DownloadServices theServices = services.get();
            switch(msg.what){
                case DOWNLOAD_COMPLETE:
                    //点击安装PendingIntent
                    Uri uri = Uri.fromFile(theServices.downFileThread.getApkFile());
                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
                    theServices.updatePendingIntent = PendingIntent.getActivity(theServices, 0, installIntent, 0);
                    theServices.myNotification.changeContentIntent(theServices.updatePendingIntent);
                    theServices.myNotification.notification.defaults=Notification.DEFAULT_SOUND;//铃声提醒
                    theServices.myNotification.changeNotificationText("下载完成，请点击安装！");
                    //停止服务
                    //  myNotification.removeNotification();
                    theServices.stopSelf();
                    // 自动安装
                    try {
                        Runtime runtime = Runtime.getRuntime();
                        Process process = runtime.exec("adb install -r " + theServices.filePathString);
                        process.getInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case DOWNLOAD_FAIL:
                    //下载失败
                    //  myNotification.changeProgressStatus(DOWNLOAD_FAIL);
                    theServices.myNotification.changeNotificationText("文件下载失败！");
                    theServices.stopSelf();
                    break;
                default:  //下载中
                    Log.i("service", "default"+msg.what);
                    //          myNotification.changeNotificationText(msg.what+"%");
                    theServices.myNotification.changeProgressStatus(msg.what);
            }
        }

    }

    private MyHandlder updateHandler = new MyHandlder(DownloadServices.this);

    public DownloadServices() {
        // TODO Auto-generated constructor stub
        //  mcontext=context;
        Log.i("service","DownloadServices1");

    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.i("service","onCreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.i("service","onDestroy");
        if(downFileThread!=null)
            downFileThread.interuptThread();
        stopSelf();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.i("service","onStartCommand");

        filePathString = intent.getStringExtra(AppConstants.NewApkPathKey);
        String downloadUrl = intent.getStringExtra(AppConstants.NewApkDownloadUrlKey);
        String notifyTitle = intent.getStringExtra(AppConstants.NotifyTitleKey);

        updateIntent = new Intent(this, MainActivity.class);
        PendingIntent   updatePendingIntent = PendingIntent.getActivity(this,0,updateIntent,0);
        myNotification=new MyNotification(this, updatePendingIntent, 1);

        //  myNotification.showDefaultNotification(R.drawable.ic_launcher, "测试", "开始下载");
        myNotification.showCustomizeNotification(R.mipmap.ic_launcher, notifyTitle, R.layout.update_download_notificatoin_view);

        //开启一个新的线程下载，如果使用Service同步下载，会导致ANR问题，Service本身也会阻塞
        downFileThread=new DownFileThread(updateHandler,downloadUrl,filePathString);
        new Thread(downFileThread).start();

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        Log.i("service","onStart");
        super.onStart(intent, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.i("service","onBind");
        return null;
    }

}
