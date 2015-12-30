package com.netease.nim.uikit.joycustom.snap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.netease.nim.uikit.R;

public class SnapItemAdapter extends BaseAdapter{
    private Context mContext;

    private LayoutInflater mInflater;

    private int fragment_id;

    private int local_selected_cover = -1;

    private int[] resIds;

    private boolean isCurrent = false;

    public SnapItemAdapter(Context context, int[] resIds, int fragment_id){
        this.mContext = context;
        this.resIds = resIds;
        this.fragment_id = fragment_id;
        this.mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setResIds(int[] resIds) {
        this.resIds = resIds;
    }

    public void selectCover(int selectIndex){
        this.local_selected_cover = selectIndex;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return resIds.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.snap_cover_item, null);
            holder.mImage = (ImageView)convertView.findViewById(R.id.snap_cover_imageView);
            holder.mSelector = (ImageView)convertView.findViewById(R.id.snap_cover_selected);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        if (SnapCoversSelectorFragment.selectedPagerIndex == fragment_id && position == local_selected_cover){
            holder.mSelector.setVisibility(View.VISIBLE);
        } else {
            holder.mSelector.setVisibility(View.GONE);
        }

        holder.mImage.setImageResource(resIds[position]);

        return convertView;
    }

    private class ViewHolder{
        private ImageView mImage;
        private ImageView mSelector;
    }
}
