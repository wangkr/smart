package com.cqyw.smart.main.util;

import android.text.TextUtils;
import android.widget.ImageView;

import com.baidu.location.BDLocation;
import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.main.activity.MainActivity;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.cqyw.smart.main.model.PublishSnapMessage;
import com.cqyw.smart.main.model.RefreshEnum;
import com.cqyw.smart.main.service.MySnapMessageDBService;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;


import java.io.File;
import java.util.List;

/**
 * Created by Kairong on 2015/11/16.
 * mail:wangkrhust@gmail.com
 */
public class MainUtils {
    private static MySnapMessageDBService mySnapMessageDBService;

    static {
        if (mySnapMessageDBService == null) {
            mySnapMessageDBService = new MySnapMessageDBService(AppContext.getContext());
        }
    }

//    /**
//     * 下载新鲜事中的附件包括阅后即焚附件-封面
//     * @param message 消息
//     */
//    public static void downloadPublicSnapMessageCover(final PublicSnapMessage message){
//        if (!TextUtils.isEmpty(message.getCover())){
//            String absUrl = UpYunImageUtil.getCoverAbsUrl(message.getCover(), ScreenUtil.coverType);
//
//            message.setAttachStatus(AttachStatusEnum.transferring);
//            x.image().loadFile(absUrl, ImageOptions.DEFAULT, new Callback.CommonCallback<File>() {
//                @Override
//                public void onSuccess(File result) {
//                    message.setAttachStatus(AttachStatusEnum.transferred);
//                    message.setCoverPath(result.getPath());
//                }
//
//                @Override
//                public void onError(Throwable ex, boolean isOnCallback) {
//                    message.setAttachStatus(AttachStatusEnum.fail);
//                    message.setCoverPath(null);
//                }
//
//                @Override
//                public void onCancelled(CancelledException cex) {
//                    message.setAttachStatus(AttachStatusEnum.fail);
//                    message.setCoverPath(null);
//                }
//
//                @Override
//                public void onFinished() {
//
//                }
//            });
//        }
//    }

    // 获取小于等于5条赞的头像的新鲜事id
    public static String getNids(List<PublicSnapMessage> messages, int anchor, int num) {
        int size = messages.size();

        if (size == 0 || anchor >= size) {
            return "";
        }
        StringBuilder nids = new StringBuilder();
        for(int i = 0; i < num && anchor + i < size;i++) {
            nids.append(messages.get(anchor + i).getId());
            nids.append(" ");
        }

        return nids.toString().trim();
    }

    public static RefreshEnum decideRefresh(String newNid, String topNid) {
        if (TextUtils.isEmpty(newNid) || TextUtils.isEmpty(topNid)) {
            return RefreshEnum.NONE;
        }

        int inewNid = Integer.valueOf(newNid);
        int itopNid = Integer.valueOf(topNid);
        int delta = inewNid - 10;
        if (delta > itopNid) {
            return RefreshEnum.NEWEAST;
        } else {
            return RefreshEnum.NEW_10;
        }
    }

    public static PublicSnapMessage createSnapMessage(PublishSnapMessage publishSnapMessage, MsgStatusEnum msgStatusEnum, AttachStatusEnum attachStatusEnum) {
        publishSnapMessage.setStatus(0);
        String nid = mySnapMessageDBService.saveNewPublishSnapMessage(publishSnapMessage);
        PublicSnapMessage snapMessage = new PublicSnapMessage(publishSnapMessage);
        snapMessage.setId(nid);
        snapMessage.setMsgStatus(msgStatusEnum);
        snapMessage.setAttachStatus(attachStatusEnum);
        snapMessage.setMessageType(1);
        snapMessage.setDirection(MsgDirectionEnum.Out);
        return snapMessage;
    }

    public static void updateMySnapMessageStatus(PublicSnapMessage publicSnapMessage, int status) {
        mySnapMessageDBService.updateMyPublicSnapMessageById(publicSnapMessage.getId(), status);
    }

    public static void deleteMySnapMessage(PublicSnapMessage publicSnapMessage) {
        mySnapMessageDBService.deleteMyPublicSnapMessageById(publicSnapMessage.getId());
    }

    public static String[] getLastKnownLocation(){
        if (MainActivity.locationManager != null) {
            String[] pos1 = new String[2];
            BDLocation location = MainActivity.locationManager.getLastKnownLocation();
            if (location != null) {
                pos1[0] = Double.toString(location.getLatitude());
                pos1[1] = Double.toString(location.getLongitude());
                return pos1;
            }
        }

        if (MainActivity.nimLocation == null) {
            String position = AppSharedPreference.getLastKnownPosition();
            if (!TextUtils.isEmpty(position)) {
                return position.split(" ");
            } else {
                return null;
            }
        } else {
            return new String[]{Double.toString(MainActivity.nimLocation.getLatitude()),
            Double.toString(MainActivity.nimLocation.getLongitude())};
        }
    }
}
