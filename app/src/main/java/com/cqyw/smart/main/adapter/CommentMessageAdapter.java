package com.cqyw.smart.main.adapter;

import android.content.Context;

import com.cqyw.smart.main.model.CommentMessage;
import com.netease.nim.uikit.common.adapter.TAdapter;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;

import java.util.List;

/**
 * 新鲜事评论适配器
 * Created by Kairong on 2015/11/15.
 * mail:wangkrhust@gmail.com
 */
public class CommentMessageAdapter extends TAdapter<CommentMessage> {
    private OnReplyClickListener onReplyClickListener;
    public CommentMessageAdapter(Context context, List<CommentMessage> items, TAdapterDelegate delegate){
        super(context, items, delegate);
    }

    public void setOnReplyClickListener(OnReplyClickListener onReplyClickListener) {
        this.onReplyClickListener = onReplyClickListener;
    }

    public OnReplyClickListener getOnReplyClickListener() {
        return onReplyClickListener;
    }

    public interface OnReplyClickListener{
        void onReplyClick(CommentMessage message);
    }
}
