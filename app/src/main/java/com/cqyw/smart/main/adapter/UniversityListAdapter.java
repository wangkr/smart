package com.cqyw.smart.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cqyw.smart.R;
import com.cqyw.smart.main.model.ListItemAttr;
import com.cqyw.smart.main.model.SchoolInfo;

import java.util.List;

/**
 * Created by Kairong on 2015/11/12.
 * mail:wangkrhust@gmail.com
 */
public class UniversityListAdapter extends BaseAdapter {

    private Context context;

    private LayoutInflater layoutInflater;

    // data
    private List<SchoolInfo> university;

    public UniversityListAdapter(Context context, List<SchoolInfo> university){
        this.context = context;
        this.university = university;

        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return university.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        if (position >= getCount()) {
            return null;
        }
        return university.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.layout_alignleft_listitem, null);
            holder.text = (TextView)convertView.findViewById(R.id.content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.text.setText(university.get(position).getSchool_name());

        return convertView;
    }

    class ViewHolder{
        private TextView text;
    }

}
