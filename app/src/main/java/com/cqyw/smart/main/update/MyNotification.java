package com.cqyw.smart.main.update;

/**
 * Notification类，既可用系统默认的通知布局，也可以用自定义的布局
 * Created by Kairong on 2015/11/9.
 * mail:wangkrhust@gmail.com
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.widget.RemoteViews;

import com.cqyw.smart.R;
import com.cqyw.smart.util.StringUtils;

public class MyNotification {
    public final static int DOWNLOAD_COMPLETE = -2;
    public final static int DOWNLOAD_FAIL = -1;

    Context mContext;   //Activity或Service上下文
    Notification notification;  //notification
    Notification.Builder builder;
    NotificationManager nm;

    String titleStr;   //通知标题
    String contentStr; //通知内容

    PendingIntent contentIntent; //点击通知后的动作
    int notificationID;   // 通知的唯一标示ID
    int iconID;           // 通知栏图标
    long when = System.currentTimeMillis();
    RemoteViews remoteView=null;  //自定义的通知栏视图
    /**
     *
     * @param context Activity或Service上下文
     * @param contentIntent  点击通知后的动作
     * @param id    通知的唯一标示ID
     */
    public MyNotification(Context context,PendingIntent contentIntent,int id) {
        // TODO Auto-generated constructor stub
        mContext=context;
        notificationID=id;
        this.contentIntent=contentIntent;
        this.nm=(NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * 显示自定义通知
     * @param icoId 自定义视图中的图片ID
     * @param titleStr 通知栏标题
     * @param layoutId 自定义布局文件ID
     */
    public void showCustomizeNotification(int icoId,String titleStr,int layoutId) {
        this.titleStr=titleStr;
        builder = new Notification.Builder(mContext)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_download_notify_msg)
                .setContentIntent(contentIntent)
                .setWhen(when);
        // 1、创建一个自定义的消息布局 view.xml
        // 2、在程序代码中使用RemoteViews的方法来定义image和text。然后把RemoteViews对象传到contentView字段
        if(remoteView==null)
        {
            remoteView = new RemoteViews(mContext.getPackageName(),layoutId);
            remoteView.setImageViewResource(R.id.update_notification_icon,icoId);
            remoteView.setTextViewText(R.id.update_notification_title, titleStr);
            remoteView.setTextViewText(R.id.update_notification_time, StringUtils.getDataTime("HH:mm"));
            remoteView.setTextViewText(R.id.update_notification_tip, "开始下载");
            remoteView.setProgressBar(R.id.update_notification_pb, 100, 0, false);
            builder.setContent(remoteView);
        }
        notification = builder.getNotification();
        nm.notify(notificationID, notification);
    }
    /**
     * 更改自定义布局文件中的进度条的值
     * @param p 进度值(0~100)
     */
    public void changeProgressStatus(int p)
    {
        if(notification.contentView!=null)
        {
            if(p==DOWNLOAD_FAIL)
                notification.contentView.setTextViewText(R.id.update_notification_tip , "下载失败！ ");
            else if(p==100)
                notification.contentView.setTextViewText(R.id.update_notification_tip , "下载完成，请点击安装");
            else
                notification.contentView.setTextViewText(R.id.update_notification_tip , "下载进度("+p+"%) : ");
            notification.contentView.setProgressBar(R.id.update_notification_pb, 100, p, false);
        }
        nm.notify(notificationID, notification);
    }
    public void changeContentIntent(PendingIntent intent)
    {
        this.contentIntent=intent;
        notification.contentIntent=intent;
    }
    /**
     * 显示系统默认格式通知
     * @param iconId 通知栏图标ID
     * @param titleText 通知栏标题
     * @param contentStr 通知栏内容
     */
    public void showDefaultNotification(int iconId,String titleText,String contentStr) {
        this.titleStr=titleText;
        this.contentStr=contentStr;
        this.iconID=iconId;

        builder = new Notification.Builder(mContext)
                .setTicker(titleStr)
                .setSmallIcon(iconID)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(contentIntent);

        notification = builder.getNotification();
        notification.flags = Notification.FLAG_INSISTENT;

        // 添加声音效果
        // notification.defaults |= Notification.DEFAULT_SOUND;

        // 添加震动,后来得知需要添加震动权限 : Virbate Permission
        // mNotification.defaults |= Notification.DEFAULT_VIBRATE ;

        //添加状态标志
        //FLAG_AUTO_CANCEL        该通知能被状态栏的清除按钮给清除掉
        //FLAG_NO_CLEAR           该通知能被状态栏的清除按钮给清除掉
        //FLAG_ONGOING_EVENT      通知放置在正在运行
        //FLAG_INSISTENT          通知的音乐效果一直播放

        changeNotificationText(contentStr);
    }
    /**
     * 改变默认通知栏的通知内容
     * @param content
     */
    public void changeNotificationText(String content)
    {

        builder.setTicker(titleStr).setContentText(content).setContentIntent(contentIntent);
        notification = builder.getNotification();


        // 设置setLatestEventInfo方法,如果不设置会App报错异常
        //  NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // 注册此通知
        // 如果该NOTIFICATION_ID的通知已存在，会显示最新通知的相关信息 ，比如tickerText 等
        nm.notify(notificationID, notification);
    }

    /**
     * 移除通知
     */
    public void removeNotification()
    {
        // 取消的只是当前Context的Notification
        nm.cancel(notificationID);
    }

}
