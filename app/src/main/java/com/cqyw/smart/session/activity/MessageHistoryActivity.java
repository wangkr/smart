package com.cqyw.smart.session.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.R;
import com.netease.nim.uikit.session.module.Container;
import com.netease.nim.uikit.session.module.ModuleProxy;
import com.netease.nim.uikit.session.module.list.MessageListPanel;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * 消息历史查询界面
 * <p/>
 * Created by huangjun on 2015/4/17.
 */
public class MessageHistoryActivity extends JActionBarActivity implements ModuleProxy {

    private static final String EXTRA_DATA_ACCOUNT = "EXTRA_DATA_ACCOUNT";
    private static final String EXTRA_DATA_SESSION_TYPE = "EXTRA_DATA_SESSION_TYPE";

    // context
    private SessionTypeEnum sessionType;
    private String account; // 对方帐号

    private MessageListPanel messageListPanel;

    public static void start(Context context, String account, SessionTypeEnum sessionType) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATA_ACCOUNT, account);
        intent.putExtra(EXTRA_DATA_SESSION_TYPE, sessionType);
        intent.setClass(context, MessageHistoryActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void initStyle() {
        super.initStyle();
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void initView() {
    }

    /**
     * ***************************** life cycle *******************************
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = LayoutInflater.from(this).inflate(R.layout.message_history_activity, null);
        setContentView(rootView);

        setTitle(R.string.message_history_query);

        onParseIntent();

        Container container = new Container(this, account, sessionType, this);
        messageListPanel = new MessageListPanel(container, rootView, true, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        messageListPanel.onDestroy();
    }

    protected void onParseIntent() {
        account = getIntent().getStringExtra(EXTRA_DATA_ACCOUNT);
        sessionType = (SessionTypeEnum) getIntent().getSerializableExtra(EXTRA_DATA_SESSION_TYPE);
    }

    @Override
    public boolean sendMessage(IMMessage msg) {
        return false;
    }

    @Override
    public void onInputPanelExpand() {

    }

    @Override
    public void shouldCollapseInputPanel() {

    }

    @Override
    public boolean isLongClickEnabled() {
        return true;
    }
}
