package com.cqyw.smart.common.network;

/**
 * Created by Kairong on 2015/11/19.
 * mail:wangkrhust@gmail.com
 */
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cqyw.smart.config.AppContext;

import java.util.ArrayList;


public class NetBroadcastReceiver extends BroadcastReceiver {
    private static ArrayList<NetEventHandler> mListeners = new ArrayList<>();
    private static String NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NET_CHANGE_ACTION)) {

            if (mListeners.size() > 0)// 通知接口完成加载
                for (NetEventHandler handler : mListeners) {
                    handler.onNetChange();
                }
        }
    }

    public static void register(NetEventHandler netEventHandler) {
        if (netEventHandler != null) {
            mListeners.add(netEventHandler);
        }
    }

    public static void unregister(NetEventHandler netEventHandler) {
        if (netEventHandler != null) {
            mListeners.remove(netEventHandler);
        }
    }

    public interface NetEventHandler {
        void onNetChange();
    }
}
