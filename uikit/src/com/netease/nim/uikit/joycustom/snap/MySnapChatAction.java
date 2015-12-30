package com.netease.nim.uikit.joycustom.snap;

import com.netease.nim.uikit.R;

import java.io.File;

/**
 * Created by Kairong on 2015/11/20.
 * mail:wangkrhust@gmail.com
 */
public class MySnapChatAction extends PickSnapCoverAction {
    public static final String TAG = MySnapChatAction.class.getSimpleName();
//    private SendSmartImageListener sendSmartImageListener;

    public MySnapChatAction () {
        super(R.drawable.btn_snapmessage_selector, R.string.snap_hint_text, true, false );
    }

//    public void setSendSmartImageListener(SendSmartImageListener sendSmartImageListener) {
//        this.sendSmartImageListener = sendSmartImageListener;
//    }

    @Override
    protected void onSnapPicked(File hiddenFile, String snapCoverName, String contentText) {
//        LogUtil.d(TAG, "onSnapPicked hiddenFile="+hiddenFile.getPath()+" snapCoverName="+snapCoverName);
//        SnapChatAttachment snapChatAttachment = new SnapChatAttachment();
//        snapChatAttachment.setSize(file.length());
//        String upyunName = UpYunImageUtil.genJoyyunFilenameFromLocalPath(file.getPath());
//        String upyunUrlName = UpYunImageUtil.genSmartImageRltPath(upyunName);
//        snapChatAttachment.setUrl(upyunUrlName);
//
//        CustomMessageConfig config = new CustomMessageConfig();
//        config.enableHistory = true;
//        config.enableRoaming = true;
//        config.enableSelfSync = true;
//        IMMessage stickerMessage = MessageBuilder.createCustomMessage(getAccount(), getSessionType(), "阅后即焚消息", snapChatAttachment, config);
//        //上传到UpYun
//        UpYunImageUtil.uploadIMMessageSmart(file, stickerMessage, upyunUrlName);
//        sendMessage(stickerMessage);
//        if (sendSmartImageListener !=null) {
//            sendSmartImageListener.onSmartSelected(message, hiddenFile);
//        }
    }
//    public interface SendSmartImageListener {
//        void onSmartSelected(PublishSnapMessage message, File smartFile);
//    }
}
