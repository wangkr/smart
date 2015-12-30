package com.cqyw.smart.config;

import com.cqyw.smart.R;

public class JoyServers {

    //
    // 好友列表信息服务器地址
    //
    private static String JOY_SERVER;

    public static void init() {
        JOY_SERVER = AppContext.getContext().getString(R.string.joy_server);
    }

    public static final String joyServer(){
        return JOY_SERVER;
    }
}
