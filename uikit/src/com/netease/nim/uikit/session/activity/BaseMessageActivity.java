package com.netease.nim.uikit.session.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.session.SessionCustomization;
import com.netease.nim.uikit.session.constant.Extras;
import com.netease.nim.uikit.session.fragment.MessageFragment;

/**
 * Created by zhoujianghua on 2015/9/10.
 */
public abstract class BaseMessageActivity extends TActionBarActivity {

    protected String sessionId;

    private SessionCustomization customization;

    protected String chatName;

    protected String chatUniversity;

    private MessageFragment messageFragment;

    private RelativeLayout actionBar;

    private boolean firstEnter = false;

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
        if (!firstEnter) {
            firstEnter = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (firstEnter) {
            finish();
        }
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

        if (customization != null) {
            customization.onActivityResult(this, requestCode, resultCode, data);
        }
    }

    private void parseIntent() {
        sessionId = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        customization = (SessionCustomization) getIntent().getSerializableExtra(Extras.EXTRA_CUSTOMIZATION);

        if (customization != null) {
            initCustomActionBar(this, customization);
        }
    }


    public void setChatBarTitle(){// Kyrong
        ((TextView)actionBar.findViewById(R.id.chatName)).setText(chatName);
        ((TextView)actionBar.findViewById(R.id.chatUniversity)).setText(chatUniversity);
    }

    // 添加action bar的右侧按钮及响应事件
    private void initCustomActionBar(TActionBarActivity activity, final SessionCustomization customization) {
        actionBar = (RelativeLayout)findViewById(R.id.p2pMessage_activity_actionbar);

        actionBar.findViewById(R.id.chatBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseMessageActivity.this.onBackPressed();
            }
        });
    }

}
