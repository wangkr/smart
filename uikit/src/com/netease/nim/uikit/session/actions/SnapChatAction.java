package com.netease.nim.uikit.session.actions;


import com.netease.nim.uikit.R;
import com.netease.nim.uikit.session.extension.SnapChatAttachment;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.File;


/**
 * Created by zhoujianghua on 2015/7/31.
 */
public class SnapChatAction extends PickImageAction {
    private SendSmartImageListener sendSmartImageListener;
    public SnapChatAction() {
        super(R.drawable.btn_snapmessage_selector, R.string.snap_chat, false);
    }

    public void setSendSmartImageListener(SendSmartImageListener sendSmartImageListener) {
        this.sendSmartImageListener = sendSmartImageListener;
    }

    @Override
    protected void onPicked(final File file) {
        SnapChatAttachment snapChatAttachment = new SnapChatAttachment();
        snapChatAttachment.setSize(file.length());
        String upyunName = JoyImageUtil.genJoyyunFilenameFromLocalPath(file.getPath());
        String upyunUrlName = JoyImageUtil.genSmartImageRltPath(upyunName);
        snapChatAttachment.setUrl(upyunUrlName);

        CustomMessageConfig config = new CustomMessageConfig();
        config.enableHistory = false;
        config.enableRoaming = false;
        config.enableSelfSync = false;
        IMMessage stickerMessage = MessageBuilder.createCustomMessage(getAccount(), getSessionType(), "阅后即焚消息", snapChatAttachment, config);
        if (sendSmartImageListener != null) {
            sendSmartImageListener.onSmartSelected(stickerMessage, file);
        }
    }


    public interface SendSmartImageListener {
        void onSmartSelected(IMMessage message, File smartFile);
    }
}
