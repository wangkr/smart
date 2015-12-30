package com.cqyw.smart.main.viewholder;

import android.text.TextUtils;

import com.cqyw.smart.main.model.PublicSnapMessage;

import java.util.HashMap;

/**
 * 显示广场新鲜事的ViewHolder工厂类
 * Created by Kairong on 2015/11/16.
 * mail:wangkrhust@gmail.com
 */
public class PublicMsgViewHolderFactory {
    public static final int USER_NEWS = 0;
    public static final int SYS_NEWS = 2;
    private static HashMap<String, Class<? extends PublicMsgViewHolderBase>> viewHolders = new HashMap<>();

    static {
        // built in
        register(PublicMsgViewHolder.class.getSimpleName(), PublicMsgViewHolder.class);
        register(PublicSysMsgViewHolder.class.getSimpleName(), PublicSysMsgViewHolder.class);
    }

    public static void register(String tag, Class<? extends PublicMsgViewHolderBase> viewHolder) {
        viewHolders.put(tag, viewHolder);
    }

    public static Class<? extends PublicMsgViewHolderBase> getViewHolderByType(PublicSnapMessage message) {
        if (message != null) {
            if (message.getType() == SYS_NEWS) {
                return viewHolders.get("PublicSysMsgViewHolder");
            } else {
                return viewHolders.get("PublicMsgViewHolder");
            }
        }
        return viewHolders.get("PublicMsgViewHolder");
    }

    public static int getViewTypeCount() {
        // plus text and unknown
        return viewHolders.size();
    }
}
