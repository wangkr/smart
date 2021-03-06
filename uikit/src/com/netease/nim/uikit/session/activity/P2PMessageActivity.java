package com.netease.nim.uikit.session.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.session.SessionCustomization;
import com.netease.nim.uikit.session.constant.Extras;
import com.netease.nim.uikit.session.fragment.MessageFragment;
import com.netease.nim.uikit.uinfo.UserInfoHelper;
import com.netease.nim.uikit.uinfo.UserInfoObservable;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.List;
import java.util.Map;


/**
 * 点对点聊天界面
 * <p/>
 * Created by huangjun on 2015/2/1.
 */
public class P2PMessageActivity extends BaseMessageActivity {

    public static void start(Context context, String contactId, SessionCustomization customization) {
        Intent intent = new Intent();
        intent.putExtra(Extras.EXTRA_ACCOUNT, contactId);
        intent.setClass(context, P2PMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 单聊特例话数据，包括个人信息
        requestBuddyInfo();
        registerObservers(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }


    private void requestBuddyInfo() {
        chatName = UserInfoHelper.getUserName(sessionId);
        chatUniversity = UserInfoHelper.getUserUniversity(sessionId);
        setChatBarTitle();
    }

    private void registerObservers(boolean register) {
        if (register) {
            registerUserInfoObserver();
        } else {
            unregisterUserInfoObserver();
        }
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(commandObserver, register);
    }

    private UserInfoObservable.UserInfoObserver uinfoObserver;
    private void registerUserInfoObserver() {
        if (uinfoObserver == null) {
            uinfoObserver = new UserInfoObservable.UserInfoObserver() {
                @Override
                public void onUserInfoChanged(List<String> accounts) {
                    if (accounts.contains(sessionId)) {
                        requestBuddyInfo();
                    }
                }
            };
        }

        UserInfoHelper.registerObserver(uinfoObserver);
    }

    private void unregisterUserInfoObserver() {
        if (uinfoObserver != null) {
            UserInfoHelper.unregisterObserver(uinfoObserver);
        }
    }

    /**
     * 命令消息接收观察者
     */
    Observer<CustomNotification> commandObserver = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification message) {
            if (!sessionId.equals(message.getSessionId()) || message.getSessionType() != SessionTypeEnum.P2P) {
                return;
            }
            showCommandMessage(message);
        }
    };

    protected void showCommandMessage(CustomNotification message) {
        String content = message.getContent();
        try {
            JSONObject json = JSON.parseObject(content);
            int id = json.getIntValue("id");
            if (id == 1) {
                // 正在输入
                Toast.makeText(P2PMessageActivity.this, "对方正在输入...", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(P2PMessageActivity.this, "command: " + content, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {

        }
    }

    @Override
    protected MessageFragment fragment() {
        Bundle arguments = getIntent().getExtras();
        arguments.putSerializable(Extras.EXTRA_TYPE, SessionTypeEnum.P2P);
        MessageFragment fragment = new MessageFragment();
        fragment.setArguments(arguments);
        fragment.setContainerId(R.id.message_fragment_container);
        return fragment;
    }

    @Override
    protected int getContentViewId() {
        return R.layout.nim_message_activity;
    }

    private IMMessage friendProfileClickMsg = new IMMessage() {
        @Override
        public String getUuid() {
            return null;
        }

        @Override
        public boolean isTheSame(IMMessage imMessage) {
            return false;
        }

        @Override
        public String getSessionId() {
            return sessionId;
        }

        @Override
        public SessionTypeEnum getSessionType() {
            return null;
        }

        @Override
        public MsgTypeEnum getMsgType() {
            return null;
        }

        @Override
        public MsgStatusEnum getStatus() {
            return null;
        }

        @Override
        public void setStatus(MsgStatusEnum msgStatusEnum) {

        }

        @Override
        public void setDirect(MsgDirectionEnum msgDirectionEnum) {

        }

        @Override
        public MsgDirectionEnum getDirect() {
            return null;
        }

        @Override
        public void setContent(String s) {

        }

        @Override
        public String getContent() {
            return null;
        }

        @Override
        public long getTime() {
            return 0;
        }

        @Override
        public void setFromAccount(String s) {

        }

        @Override
        public String getFromAccount() {
            return sessionId;
        }

        @Override
        public void setAttachment(MsgAttachment msgAttachment) {

        }

        @Override
        public MsgAttachment getAttachment() {
            return null;
        }

        @Override
        public AttachStatusEnum getAttachStatus() {
            return null;
        }

        @Override
        public void setAttachStatus(AttachStatusEnum attachStatusEnum) {

        }

        @Override
        public CustomMessageConfig getConfig() {
            return null;
        }

        @Override
        public void setConfig(CustomMessageConfig customMessageConfig) {

        }

        @Override
        public Map<String, Object> getRemoteExtension() {
            return null;
        }

        @Override
        public void setRemoteExtension(Map<String, Object> map) {

        }

        @Override
        public Map<String, Object> getLocalExtension() {
            return null;
        }

        @Override
        public void setLocalExtension(Map<String, Object> map) {

        }

        @Override
        public String getPushContent() {
            return null;
        }

        @Override
        public void setPushContent(String s) {

        }

        @Override
        public Map<String, Object> getPushPayload() {
            return null;
        }

        @Override
        public void setPushPayload(Map<String, Object> map) {

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nim_message_activity_menu) {
            if (NimUIKit.getSessionListener() != null) {
                NimUIKit.getSessionListener().onAvatarClicked(this, friendProfileClickMsg);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nim_message_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
