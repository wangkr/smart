package com.cqyw.smart.session.viewholder;

import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqyw.smart.R;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.contact.constant.UserConstant;
import com.cqyw.smart.main.activity.WatchSnapPublicSmartActivity;
import com.cqyw.smart.util.Utils;
import com.netease.nim.uikit.session.activity.WatchSnapChatSmartActivity;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.session.constant.RequestCode;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderBase;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;


/**
 * Created by zhoujianghua on 2015/8/7.
 */
public class MsgViewHolderSnapChat extends MsgViewHolderBase {

    private ImageView thumbnailImageView;

    protected View progressCover;
    private TextView progressLabel;
    private TextView tipsLable;

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_snapchat;
    }

    @Override
    protected void inflateContentView() {
        thumbnailImageView = (ImageView) view.findViewById(R.id.message_item_snap_chat_image);
        progressBar = findViewById(R.id.message_item_thumb_progress_bar); // 覆盖掉
        progressCover = findViewById(R.id.message_item_thumb_progress_cover);
        progressLabel = (TextView) view.findViewById(R.id.message_item_thumb_progress_text);
        tipsLable = findViewById(R.id.message_item_snap_chat_tips_label);
    }

    @Override
    protected void bindContentView() {
//        contentContainer.setOnTouchListener(onTouchListener);

        layoutByDirection();

        refreshStatus();
    }

    private void refreshStatus() {
//        String json = ((FileAttachment)message.getAttachment()).getExtension();
//        thumbnailImageView.setImageResource(R.drawable.msg_right_transparent_bg);

        // 置消息状态
        if (TimeUtil.isEarly(1, message.getTime())) {
            message.setStatus(MsgStatusEnum.read);
        }

        // 置已读/未读标记
        if (message.getStatus() == MsgStatusEnum.read ) {
            tipsLable.setEnabled(false);
        } else if (message.getStatus() == MsgStatusEnum.unread) {
            tipsLable.setEnabled(true);
        }

        if (message.getStatus() == MsgStatusEnum.sending || message.getAttachStatus() == AttachStatusEnum.transferring) {
            progressCover.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressCover.setVisibility(View.GONE);
        }

        progressLabel.setText(StringUtil.getPercentString(getAdapter().getProgress(message)));
    }

//    protected View.OnTouchListener onTouchListener = new View.OnTouchListener() {
//
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            switch (event.getAction()) {
//            case MotionEvent.ACTION_MOVE:
//                v.getParent().requestDisallowInterceptTouchEvent(true);
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                v.getParent().requestDisallowInterceptTouchEvent(false);
//                boolean hasSeen = WatchSnapChatSmartActivity.hasSeen;
//                WatchSnapChatSmartActivity.destroy();
//
//                // 将其标记为已读，同时删除附件内容，然后不让再查看
//                if (isLongClick && hasSeen) {
//                    // 物理删除
//                    message.setStatus(MsgStatusEnum.read);
//                    // 置已读标记
//                    tipsLable.setEnabled(false);// Kyrong
//                    isLongClick = false;
//                }
//                break;
//            }
//
//            return false;
//        }
//    };

    @Override
    protected boolean onItemLongClick() {
        // 查看阅后即焚
        if (!TimeUtil.isEarly(1, message.getTime())) {// 没有过期
            // 自己可以永久查看
            if (message.getDirect() == MsgDirectionEnum.Out) {
                WatchSnapChatSmartActivity.start(context, message, RequestCode.WATCH_SNAP_CHAT);
                return true;
            } else if (message.getStatus() == MsgStatusEnum.success) {
                WatchSnapChatSmartActivity.start(context, message, RequestCode.WATCH_SNAP_CHAT);
                return true;
            } else if (message.getStatus() == MsgStatusEnum.read) {
                Utils.showLongToast(context, "只能查看一次,您已经查看过了");
                return false;
            }
        } else {
            Utils.showLongToast(context, "图片超过24小时,已销毁");
        }
        return false;
    }

    @Override
    protected int leftBackground() {
        return R.drawable.msg_left_selector;
    }

    @Override
    protected int rightBackground() {
        return R.drawable.msg_right_selector;
    }

    private void layoutByDirection() {
        View body = findViewById(R.id.message_item_snap_chat_body);
        View tips = findViewById(R.id.message_item_snap_chat_tips_label);
        ViewGroup container = (ViewGroup) body.getParent();
        container.removeView(tips);
        if (isReceivedMessage()) {
            container.addView(tips, 1);
        } else {
            container.addView(tips, 0);
        }
        if (message.getStatus() == MsgStatusEnum.success) {
            tips.setVisibility(View.VISIBLE);
        } else {
            tips.setVisibility(View.GONE);
        }
    }
}
