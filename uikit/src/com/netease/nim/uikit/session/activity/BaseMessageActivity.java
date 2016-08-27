package com.netease.nim.uikit.session.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.session.SessionCustomization;
import com.netease.nim.uikit.session.constant.Extras;
import com.netease.nim.uikit.session.fragment.MessageFragment;
import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * Created by zhoujianghua on 2015/9/10.
 */
public abstract class BaseMessageActivity extends TActionBarActivity {

    protected String sessionId;

//    private SessionCustomization customization;

    protected String chatName;

    protected String chatUniversity;

    private MessageFragment messageFragment;

    private ActionBar actionBar;

    protected abstract MessageFragment fragment();
    protected abstract int getContentViewId();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getContentViewId());
        parseIntent();

        messageFragment = (MessageFragment) switchContent(fragment());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (messageFragment == null || !messageFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (messageFragment != null) {
            messageFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void parseIntent() {
        sessionId = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        initCustomActionBar();
    }


    public void setChatBarTitle(){// Kyrong
        if (actionBar != null) {
            setTitle(chatName);
            setSubTitle(chatUniversity);
        }
    }

    // 添加action bar的右侧按钮及响应事件
    private void initCustomActionBar() {
        actionBar = getSupportActionBar();
    }

}
