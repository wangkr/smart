package com.cqyw.smart.session.action;


import com.cqyw.smart.R;
import com.cqyw.smart.session.extension.SnapChatAttachment;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.netease.nim.uikit.session.actions.PickImageAction;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.File;


/**
 * Created by zhoujianghua on 2015/7/31.
 */
public class SnapChatAction extends PickImageAction {

    public SnapChatAction() {
        super(com.netease.nim.uikit.R.drawable.btn_snapmessage_selector, R.string.input_panel_snapchat, false);
    }

    @Override
    protected void onPicked(final File file) {
        SnapChatAttachment snapChatAttachment = new SnapChatAttachment();
        snapChatAttachment.setSize(file.length());
        String upyunName = JoyImageUtil.genJoyyunFilenameFromLocalPath(file.getPath());
        String upyunUrlName = JoyImageUtil.genSmartImageRltPath(upyunName);
        snapChatAttachment.setUrl(upyunUrlName);

        CustomMessageConfig config = new CustomMessageConfig();
        config.enableHistory = true;
        config.enableRoaming = true;
        config.enableSelfSync = true;
        IMMessage stickerMessage = MessageBuilder.createCustomMessage(getAccount(), getSessionType(), "阅后即焚消息", snapChatAttachment, config);
        //上传到UpYun
        JoyImageUtil.uploadIMMessageSmart(file, stickerMessage, upyunUrlName);
        sendMessage(stickerMessage);
    }

}
