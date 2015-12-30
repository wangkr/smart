package com.cqyw.smart.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.cqyw.smart.main.model.PublicSnapMessage;
import com.netease.nim.uikit.common.adapter.TAdapter;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 广场阅后即焚消息adapter
 * Created by Kairong on 2015/10/11.
 * mail:wangkrhust@gmail.com
 */
public class PublicSnapMsgAdapter extends TAdapter<PublicSnapMessage> {
    private ViewHolderEventListener eventListener;

    public PublicSnapMsgAdapter(Context context, List<PublicSnapMessage> items, TAdapterDelegate delegate){
        super(context, items, delegate);
    }

    public void setEventListener(ViewHolderEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public ViewHolderEventListener getEventListener() {
        return eventListener;
    }

    public void deleteItem(PublicSnapMessage message) {
        if (message == null) {
            return;
        }

        int index = 0;
        for (PublicSnapMessage item : getItems()) {
            if (item.isTheSame(message)) {
                break;
            }
            ++index;
        }

        if (index < getCount()) {
            getItems().remove(index);
            notifyDataSetChanged();
        }
    }

    public interface ViewHolderEventListener {
        // 长按事件响应处理
        boolean onViewHolderLongClick(View clickView, View viewHolderView, PublicSnapMessage item);

        // 发送失败或者多媒体文件下载失败指示按钮点击响应处理
        void onFailedBtnClick(PublicSnapMessage resendMessage);

        void onDeleteBtnClick(PublicSnapMessage deleteMsg);
    }
}
