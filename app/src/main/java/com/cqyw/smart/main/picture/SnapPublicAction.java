package com.cqyw.smart.main.picture;

import com.cqyw.smart.R;
import com.cqyw.smart.main.model.PublishSnapMessage;
import com.cqyw.smart.main.util.MainUtils;
import com.netease.nim.uikit.joycustom.snap.PickSnapCoverAction;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;

import java.io.File;

/**
 * Created by Kairong on 2015/11/20.
 * mail:wangkrhust@gmail.com
 */
public class SnapPublicAction extends PickSnapCoverAction {
    public static final String TAG = SnapPublicAction.class.getSimpleName();
    private SendSmartImageListener sendSmartImageListener;

    public SnapPublicAction () {
        super(R.drawable.btn_snap_picture_large, R.string.input_panel_snapchat, true, true );
    }

    public void setSendSmartImageListener(SendSmartImageListener sendSmartImageListener) {
        this.sendSmartImageListener = sendSmartImageListener;
    }

    @Override
    protected void onSnapPicked(File hiddenFile, String snapCoverName, String contentText) {
        PublishSnapMessage message = new PublishSnapMessage();
        String upyunName = JoyImageUtil.genJoyyunFilenameFromLocalPath(hiddenFile.getPath());
        String upyunUrlName = JoyImageUtil.genSmartImageRltPath(upyunName);
        message.setSmart(upyunUrlName);
        message.setCover(snapCoverName);
        message.setContent(contentText);

        String pos[] = MainUtils.getLastKnownLocation();
        if (pos != null) {
            message.setLat(pos[0]);
            message.setLng(pos[1]);
            if (sendSmartImageListener != null) {
                sendSmartImageListener.onSmartSelected(message, hiddenFile);
            }
        } else {
            if (sendSmartImageListener != null) {
                sendSmartImageListener.onSmartSelected(null, hiddenFile);
            }
        }
    }
    public interface SendSmartImageListener {
        void onSmartSelected(PublishSnapMessage message, File smartFile);
    }
}
