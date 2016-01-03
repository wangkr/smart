package com.cqyw.smart.friend.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.contact.ExtensionParse;
import com.cqyw.smart.contact.activity.UserProfileActivity;
import com.cqyw.smart.contact.extensioninfo.ExtensionInfoCache;
import com.cqyw.smart.contact.extensioninfo.model.ExtensionInfo;
import com.cqyw.smart.contact.helper.ExtInfoHelper;
import com.cqyw.smart.friend.helper.MessageHelper;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.constant.SystemMessageStatus;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * Created by huangjun on 2015/3/18.
 */
public class SystemMessageViewHolder extends TViewHolder {
    public static final String TAG = SystemMessageViewHolder.class.getSimpleName();

    private SystemMessage message;
    private HeadImageView headImageView;
    private TextView fromAccountText;
    private TextView timeText;
    private TextView contentText;
    private View operatorLayout;
    private Button agreeButton;
//    private Button rejectButton;
    private TextView operatorResultText;
    private SystemMessageListener listener;

    // user info
    private NimUserInfo nimUserInfo;

    public interface SystemMessageListener {
        void onAgree(SystemMessage message);

        void onReject(SystemMessage message);

        void onLongPressed(SystemMessage message);
    }

    @Override
    protected int getResId() {
        return R.layout.layout_newfriend_item;
    }

    @Override
    protected void inflate() {
        headImageView = (HeadImageView) view.findViewById(R.id.from_account_head_image);
        fromAccountText = (TextView) view.findViewById(R.id.from_account_text);
        contentText = (TextView) view.findViewById(R.id.content_text);
        timeText = (TextView) view.findViewById(R.id.notification_time);
        operatorLayout = view.findViewById(R.id.operator_layout);
        agreeButton = (Button) view.findViewById(R.id.agree);
//        rejectButton = (Button) view.findViewById(R.id.reject);
        operatorResultText = (TextView) view.findViewById(R.id.operator_result);
        view.setBackgroundResource(R.drawable.list_item_bg_selecter);
    }
    // 设置用户名
    private void setFromAccountText(){
        String fromName = NimUserInfoCache.getInstance().getUserDisplayNameEx(message.getFromAccount());
        if (TextUtils.isEmpty(fromName)) {
            NimUserInfoCache.getInstance().getUserInfoFromRemote(message.getFromAccount(), new RequestCallback<NimUserInfo>() {
                @Override
                public void onSuccess(NimUserInfo userInfo) {
                    fromAccountText.setText(userInfo.getName());
                }

                @Override
                public void onFailed(int i) {
                    fromAccountText.setText("未知用户");
                }

                @Override
                public void onException(Throwable throwable) {

                }
            });
        } else {
            fromAccountText.setText(fromName);
        }
    }

    @Override
    protected void refresh(Object item) {
        message = (SystemMessage) item;
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onLongPressed(message);
                }

                return true;
            }
        });
        headImageView.loadBuddyAvatar(message.getFromAccount());
        setFromAccountText();
        contentText.setText(MessageHelper.getVerifyNotificationText(message));
        timeText.setText(TimeUtil.getTimeShowString(message.getTime(), false));
        if (!MessageHelper.isVerifyMessageNeedDeal(message)) {
            operatorLayout.setVisibility(View.GONE);
        } else {
            if (message.getStatus() == SystemMessageStatus.init) {
                // 未处理
                operatorResultText.setVisibility(View.GONE);
                agreeButton.setVisibility(View.VISIBLE);
//                rejectButton.setVisibility(View.VISIBLE);
            } else {
                // 处理结果
                agreeButton.setVisibility(View.GONE);
//                rejectButton.setVisibility(View.GONE);
                operatorResultText.setVisibility(View.VISIBLE);
                operatorResultText.setText(MessageHelper.getVerifyNotificationDealResult(message));
            }
        }
    }

    public void refreshDirectly(final SystemMessage message) {
        if (message != null) {
            refresh(message);
        }
    }

    public void setListener(final SystemMessageListener l) {
        if (l == null) {
            return;
        }

        this.listener = l;
        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReplySending();
                listener.onAgree(message);
                agreeAddFriendRequest(message);
            }
        });

//        rejectButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setReplySending();
//                listener.onReject(message);
//            }
//        });

        headImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileActivity.start(context, message.getFromAccount());
            }
        });
    }



    /**
     * 等待服务器返回状态设置
     */
    private void setReplySending() {
        agreeButton.setVisibility(View.GONE);
//        rejectButton.setVisibility(View.GONE);
        operatorResultText.setVisibility(View.VISIBLE);
        operatorResultText.setText(R.string.team_apply_sending);
    }

    // 获取扩展信息：包括newsno, photos, jo
    ICommProtocol.CommCallback<ExtensionInfo> extInfoCallback = new ICommProtocol.CommCallback<ExtensionInfo>() {
        @Override
        public void onSuccess(ExtensionInfo extensionInfo) {
            ExtensionInfoCache.saveExtensionInfo(message.getFromAccount(), extensionInfo);
            ExtInfoHelper.initFriendNote(message.getFromAccount(), nimUserInfo.getName(), StringUtil.Empty,
                    ExtensionParse.getInstance().getUniversity(nimUserInfo.getExtension()), extensionInfo.getJo());
        }

        @Override
        public void onFailed(String code, String errorMsg) {
            ExtInfoHelper.initFriendNote(message.getFromAccount(), nimUserInfo.getName(), StringUtil.Empty,
                    ExtensionParse.getInstance().getUniversity(nimUserInfo.getExtension()), "0");
        }
    };

    RequestCallback<NimUserInfo> initNotecallback = new RequestCallback<NimUserInfo>() {
        @Override
        public void onSuccess(NimUserInfo userInfo) {
            if (userInfo == null) {
                return;
            }
            nimUserInfo = userInfo;
            JoyCommClient.getInstance().getExtensionInfo(AppCache.getJoyId(), AppCache.getJoyToken(), message.getFromAccount(), extInfoCallback);
        }

        @Override
        public void onFailed(int i) {

        }

        @Override
        public void onException(Throwable throwable) {

        }
    };

    private void agreeAddFriendRequest(final SystemMessage message){
        // 告诉本地服务器添加好友成功
        JoyCommClient.getInstance().addFriendInfo2Server(AppCache.getJoyId(), message.getFromAccount(), AppSharedPreference.getCacheJoyToken(), new ICommProtocol.CommCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "addFriendInfo2Server successful!");
            }

            @Override
            public void onFailed(String code, String errorMsg) {
                LogUtil.d(TAG, "addFriendInfo2Server failed:" + errorMsg + "code:" + code);
            }
        });
        // 初始化备注表
        nimUserInfo = NimUserInfoCache.getInstance().getUserInfo(message.getFromAccount());
        if (nimUserInfo != null) {
            JoyCommClient.getInstance().getExtensionInfo(AppCache.getJoyId(), AppCache.getJoyToken(), message.getFromAccount(), extInfoCallback);
        } else {
            NimUserInfoCache.getInstance().getUserInfoFromRemote(message.getFromAccount(), initNotecallback);
        }

    }
}
