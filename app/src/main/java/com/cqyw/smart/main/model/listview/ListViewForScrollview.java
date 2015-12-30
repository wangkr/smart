package com.cqyw.smart.main.model.listview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by Kairong on 2015/11/24.
 * mail:wangkrhust@gmail.com
 */
public class ListViewForScrollview extends ListView {

    public ListViewForScrollview(Context context) {
        super(context);
    }
    public ListViewForScrollview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ListViewForScrollview(Context context, AttributeSet attrs,
                                 int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
