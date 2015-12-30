package com.cqyw.smart.main.adapter;

import android.content.Context;
import android.view.View;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cqyw.smart.main.model.RecentSnapNews;
import com.netease.nim.uikit.common.adapter.TAdapter;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;

import java.util.List;

/**
 * Created by Kairong on 2015/11/15.
 * mail:wangkrhust@gmail.com
 */
public class RecentSnapNewsAdapter extends TAdapter<RecentSnapNews> {
    private ViewHolderEventListener eventListener;

    public RecentSnapNewsAdapter(Context context, List<RecentSnapNews> items, TAdapterDelegate delegate){
        super(context, items, delegate);
    }

    public void setEventListener(ViewHolderEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public ViewHolderEventListener getEventListener() {
        return eventListener;
    }

    public void deleteItem(RecentSnapNews message) {
        if (message == null) {
            return;
        }

        int index = 0;
        for (RecentSnapNews item : getItems()) {
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
        boolean onViewHolderLongClick(View clickView, View viewHolderView, RecentSnapNews item);
    }
}
