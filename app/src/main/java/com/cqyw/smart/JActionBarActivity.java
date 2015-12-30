package com.cqyw.smart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.util.sys.ActionBarUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Kairong on 2015/10/13.
 * mail:wangkrhust@gmail.com
 */
public abstract class JActionBarActivity extends TActionBarActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStyle();
        initData();
        initView();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    /**
     * 初始化数据信息
     */
    protected abstract void initData();

    /**
     * 初始化视图信息
     */
    protected abstract void initView();


    /**
     * 初始化Activity样式
     */
    protected void initStyle() {
        // empty
    }

    /**
     * 设置actionBar文字菜单样式
     * @param titleResId
     * @param context
     */
    public void setMenuClickableTxt(TActionBarActivity context, int titleResId, View.OnClickListener onClickListener){
        ActionBarUtil.addRightClickableTextViewOnActionBar(context, titleResId, onClickListener);
    }

    /**
     * 设置action bar 的菜单状态
     * @param enabled
     */
    public void setMenuTextEnabled(TActionBarActivity activity, boolean enabled){
        ActionBarUtil.setTextViewEnable(activity, enabled);
    }

    public void setMenuTextVisible(TActionBarActivity activity, boolean visible) {
        ActionBarUtil.setTextViewVisible(activity, visible);
    }


    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
