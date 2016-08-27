package com.netease.nim.uikit.common.media.picker.joycamera;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.picker.joycamera.model.CamOnLineRes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kairong on 2016/8/7.
 * mail:wangkrhust@gmail.com
 */
public class ThumbImageAdapter extends RecyclerView.Adapter<ThumbImageHolder> {
    private List<CamOnLineRes> items;
    private LayoutInflater inflater;
    private ThumbImageHolder.OnThumbClickEvent onThumbClickEvent;
    private Object mLock = new Object();
    public ThumbImageAdapter(Context context, List<CamOnLineRes> items, ThumbImageHolder.OnThumbClickEvent onThumbClickEvent) {
        this.items = items;
        this.inflater = LayoutInflater.from(context);
        this.onThumbClickEvent = onThumbClickEvent;
    }

    @Override
    public void onBindViewHolder(ThumbImageHolder holder, int position) {
        holder.refresh(getItem(position), position, onThumbClickEvent);
    }

    protected CamOnLineRes getItem(final int pos) {
        synchronized (mLock) {
            return items.get(pos);
        }
    }

    @Override
    public ThumbImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.thumb_icon_horizontal_list_item, parent, false);
        return new ThumbImageHolder(view, onThumbClickEvent);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Insert a item to the list of the adapter
     *
     * @param object   object T
     * @param position position
     */
    public final void insertInternal(CamOnLineRes object, final int position) {
        items.add(position, object);
        notifyItemInserted(position);
    }


    public final void insertFirstInternal(CamOnLineRes item) {
        insertInternal(item, 0);
    }

    public final void insertLastInternal(CamOnLineRes item) {
        insertInternal(item, getItemCount());
    }

    /**
     * Remove a item of  the list of the adapter
     *
     * @param position na
     */
    public final void removeInternal(int position) {
        if (items.size() > 0) {
            synchronized (mLock) {
                items.remove(position);
            }
            notifyItemRemoved(position);
        }
    }


    public final void removeFirstInternal() {
        removeInternal(0);
    }

    public final void removeLastInternal() {
        removeInternal(getItemCount() - 1);
    }
}
