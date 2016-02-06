package com.netease.nim.uikit.common.ui.widget.squareprogressbar;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Kairong on 2015/12/13.
 * mail:wangkrhust@gmail.com
 */
public class MyImageView extends ImageView {
    public MyImageView (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
