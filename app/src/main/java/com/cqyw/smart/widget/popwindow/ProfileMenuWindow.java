package com.cqyw.smart.widget.popwindow;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.cqyw.smart.R;

/**
 * 用户资料弹出窗口
 * Created by Kairong on 2015/10/14.
 * mail:wangkrhust@gmail.com
 */
public class ProfileMenuWindow extends PopupWindow {
    Context mContext;
    LayoutInflater mLayoutInflater;
    LinearLayout mContentView;
    View.OnClickListener mClickListener;
    int[] profile_menu_ids;
    TextView[] menuItem;
    public ProfileMenuWindow(Context context){
        super(context);
        mContext = context;
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = (LinearLayout)mLayoutInflater.inflate(R.layout.layout_profile_menu, null);
        setContentView(mContentView);
        setOutsideTouchable(true);
        setAnimationStyle(android.R.style.Animation_Dialog);
        update();
        setTouchable(true);
        setFocusable(true);
    }

    /*初始化menuItem*/
    public void initMenuItem(CharSequence[] menuItemTitle){
        menuItem = new TextView[menuItemTitle.length];
        profile_menu_ids = mContext.getResources().getIntArray(R.array.profile_menu_item_ids);
        if(menuItemTitle.length > 20){
            Toast.makeText(mContext,R.string.profile_menu_item_warning, Toast.LENGTH_LONG);
        }
        for(int i = 0;i < menuItemTitle.length && i< 20;i++){
            menuItem[i] = (TextView)mLayoutInflater.inflate(R.layout.menu_item_profile, null);
            menuItem[i].setText(menuItemTitle[i]);
            menuItem[i].setId(profile_menu_ids[i]);
            /*点击menu其他任何地方，menu都会消失*/
            menuItem[i].setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // TODO Auto-generated method stub
                    if ((keyCode == KeyEvent.KEYCODE_MENU) && (isShowing())) {
                        dismiss();// 这里写明模拟menu的PopupWindow退出就行
                        return true;
                    }
                    return false;
                }
            });
            mContentView.addView(menuItem[i]);
        }
    }

    public void setMenuOnClickListener(final View.OnClickListener onClickListener){
        mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(v);
                dismiss();
            }
        };
        if(menuItem != null && menuItem.length >0){
            for(int i = 0;i < menuItem.length;i++){
                menuItem[i].setOnClickListener(mClickListener);
            }
        }
    }


}
