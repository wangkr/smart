package com.cqyw.smart.main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cqyw.smart.R;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.cqyw.smart.main.viewholder.PublicMsgViewHolder;
import com.cqyw.smart.main.viewholder.PublicMsgViewHolderBase;
import com.cqyw.smart.main.viewholder.PublicMsgViewHolderFactory;
import com.cqyw.smart.main.viewholder.PublicSysMsgViewHolder;
import com.marshalchen.ultimaterecyclerview.SwipeableUltimateViewAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.netease.nim.uikit.common.adapter.TAdapter;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.media.picker.joycamera.model.PublishMessage;
import com.netease.nim.uikit.common.util.log.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 广场阅后即焚消息adapter
 * Created by Kairong on 2015/10/11.
 * mail:wangkrhust@gmail.com
 */
public class PublicSnapMsgAdapter extends SwipeableUltimateViewAdapter<PublicSnapMessage> {

    private List<PublicSnapMessage> data;
    private PublicMsgViewHolderBase.ViewHolderEventListener eventListener;
    private LayoutInflater inflater;

    public PublicSnapMsgAdapter(Context context, PublicMsgViewHolderBase.ViewHolderEventListener eventListener, List<PublicSnapMessage> data) {
        super(data);
        this.eventListener = eventListener;
        this.data = data;
        this.inflater = LayoutInflater.from(context);
        LogUtil.d("PSMA", "publicSnapMsgAdapter onCreate");
    }


    @Override
    protected void withBindHolder(UltimateRecyclerviewViewHolder holder, PublicSnapMessage data, int position) {
        //super.withBindHolder(holder, data, position);
        if (position < getAdapterItemCount()) {
            ((PublicMsgViewHolderBase) holder).refreshCurrentItem(getItem(position));
        } else if(position == getItemCount()-1) {

        }
        LogUtil.d("PSMA", "refresh");
    }

    @Override
    protected int getNormalLayoutResId() {
        return R.layout.layout_snap_msg_item;
    }

    @Override
    protected UltimateRecyclerviewViewHolder newViewHolder(View view) {
        LogUtil.d("PSMA", "create");
        return new PublicMsgViewHolder(view, eventListener);
    }


    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public PublicMsgViewHolderBase newHeaderHolder(View view) {
        return null;
    }

    @Override
    public PublicMsgViewHolderBase newFooterHolder(View view) {
        return new PublicMsgViewHolder(view, null);
    }

    @Override
    public PublicMsgViewHolderBase newCustomViewHolder(View view) {
        View mView = inflater.inflate(R.layout.layout_snap_msg_item, null);
        return new PublicSysMsgViewHolder(mView, eventListener);
    }

    @Override
    public int getAdapterItemCount() {
        return data.size();
    }

    @Override
    public long generateHeaderId(int position) {
        return 0;
    }


    @Override
    public int getItemViewType(int position) {
//        if (getItem(position).getType() == PublishMessage.MessageType.SYSTEM.value()){
//            return VIEW_TYPES.CUSTOMVIEW;
//        }

        return VIEW_TYPES.NORMAL;
    }

    protected PublicSnapMessage getItem(final int pos) {
        synchronized (mLock) {
            return data.get(pos);
        }
    }

    public void removeItem(PublicSnapMessage message) {
        if (message == null) {
            return;
        }

        int index = 0;
        for (PublicSnapMessage item : data) {
            if (item.isTheSame(message)) {
                break;
            }
            ++index;
        }

        if (index < getAdapterItemCount()) {
            removeAt(index);
            notifyItemChanged(index);
        }
    }

}
