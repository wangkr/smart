package com.cqyw.smart.main.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.main.model.IGetRecentSnapnews;
import com.cqyw.smart.main.model.RecentSnapNews;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 后台读取新鲜事消息
 * Created by Kairong on 2015/11/20.
 * mail:wangkrhust@gmail.com
 */
public class RecentNewsService extends Service implements Runnable{
    private static final String TAG = RecentNewsService.class.getSimpleName();
    protected boolean isRun = true;
    private final static ArrayList<Observer<List<RecentSnapNews>>> observers = new ArrayList<>();

    public class ServiceBinder extends Binder {
        public RecentNewsService getService() {
            return RecentNewsService.this;
        }
    }
    @Override
    public void onCreate() {
        LogUtil.d(TAG, "========================onCreate");
        super.onCreate();
        isRun = true;
        new Thread(this).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, START_REDELIVER_INTENT, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "========================onDestroy");
        super.onDestroy();
        isRun = false;
    }

    public void register(Observer<List<RecentSnapNews>> observer, boolean register) {
        if (register) {
            if (observer == null) {
                return;
            }
            synchronized(observers) {
                if (observers.contains(observer)) {
                    return;
                }
                observers.add(observer);
            }
        } else {
            if (observer == null) {
                return;
            }
            synchronized(observers) {
                int index = observers.indexOf(observer);
                if (index == -1) {
                    return;
                }
                observers.remove(index);
            }
        }
    }

    @Override
    public void run() {
        while (isRun) {
            JoyCommClient.getInstance().getRecentNews(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), new ICommProtocol.CommCallback<List<RecentSnapNews>>() {
                @Override
                public void onSuccess(List<RecentSnapNews> recentSnapNewses) {
                    if (recentSnapNewses.size() > 0) {
                        if (observers.size() > 0) {
                            for (Observer<List<RecentSnapNews>> observer : observers) {
                                observer.onEvent(recentSnapNewses);
                            }
                        }
                    }
                }

                @Override
                public void onFailed(String code, String errorMsg) {

                }
            });
            try {
                Thread.sleep(40*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder();
    }
}
