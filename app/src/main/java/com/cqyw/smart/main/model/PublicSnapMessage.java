package com.cqyw.smart.main.model;

import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.common.media.picker.joycamera.model.PublishMessage;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;

import java.io.Serializable;

/**
 * Created by Kairong on 2015/11/13.
 * mail:wangkrhust@gmail.com
 */
public class PublicSnapMessage implements Serializable{
    // meta data
    /*新鲜事id*/
    private String id;
    /*发布者id*/
    private String uid;
    /*新鲜事的经度*/
    private String lat;
    /*新鲜事的纬度*/
    private String lng;
    /*文字内容*/
    private String content;
    /*封面图名字*/
    private String cover;
    /*smart图名字*/
    private String smart;
    /*发布的时间*/
    private String intime;
    /*查看数量*/
    private String viewer; // added in v1.0.4
    /*封面图本地路径*/
    private String coverLocalPath;
    /*评论数量*/
    private int comment;
    /*类型*/
    private int type; // 2 系统消息
    /*赞的数量*/
    private int like;
    /*举报的状态*/
    private int report;
    /*读取状态*/
    private int read;   // 0 未读，1 已读
    /*是否过期*/
    private int status; // 0 过期，1 未过期

    // status data
    /*新鲜事状态*/
    private MsgStatusEnum msgStatus;
    /*附件状态*/
    private AttachStatusEnum attachStatus;
    /*发送消息方向*/
    private MsgDirectionEnum directionEnum;
    /*赞的头像的uid*/
    private LikeMessage likeMessage;
    /*消息类型：系统或者用户消息*/
    private int messageType = 1;// 2 代表详细信息 1 代表普通用户信息，0 代表系统信息


    public PublicSnapMessage(){}

    public PublicSnapMessage(String id, String uid, String lat, String lng, String content,String viewer,
                             String cover, String smart, String intime, int type, int comment,
                             int like, int report, int read, int status) {
        this.id = id;
        this.uid = uid;
        this.lat = lat;
        this.lng = lng;
        this.content = content;
        this.cover = cover;
        this.smart = smart;
        this.type = type;
        this.intime = intime;
        this.viewer = viewer;
        this.comment = comment;
        this.like = like;
        this.report = report;
        this.read = read;
        this.status = status;

    }

    public PublicSnapMessage(Bundle bundle){
        if (bundle == null) {
            return;
        }
        this.id = bundle.getString(SnapMsgConstant.MSG_KEY_ID);
        this.lat = bundle.getString(SnapMsgConstant.MSG_KEY_LAT);
        this.lng = bundle.getString(SnapMsgConstant.MSG_KEY_LNG);
        this.content = bundle.getString(SnapMsgConstant.MSG_KEY_CONTENT);
        this.cover = bundle.getString(SnapMsgConstant.MSG_KEY_COVER);
        this.smart = bundle.getString(SnapMsgConstant.MSG_KEY_SMART);
        this.uid = bundle.getString(SnapMsgConstant.MSG_KEY_UID);
        this.intime = bundle.getString(SnapMsgConstant.MSG_KEY_TIME);
        this.viewer = bundle.getString(SnapMsgConstant.MSG_KEY_VIEWER);

        this.type = bundle.getInt(SnapMsgConstant.MSG_KEY_TYPE);
        this.read = bundle.getInt(SnapMsgConstant.MSG_KEY_READ);
        this.report = bundle.getInt(SnapMsgConstant.MSG_KEY_REPORT);
        this.comment = bundle.getInt(SnapMsgConstant.MSG_KEY_COMMENT);
        this.like = bundle.getInt(SnapMsgConstant.MSG_KEY_LIKE);
        this.status = bundle.getInt(SnapMsgConstant.MSG_KEY_STATUS);

        this.msgStatus = MsgStatusEnum.statusOfValue(bundle.getInt("msgSts"));
        this.attachStatus = AttachStatusEnum.statusOfValue(bundle.getInt("attachSts"));
        this.directionEnum = MsgDirectionEnum.directionOfValue(bundle.getInt("direction"));
        this.messageType = bundle.getInt("msgType");

    }

    public Bundle getSaveBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(SnapMsgConstant.MSG_KEY_ID, id);
        bundle.putString(SnapMsgConstant.MSG_KEY_LAT, lat);
        bundle.putString(SnapMsgConstant.MSG_KEY_LNG, lng);
        bundle.putString(SnapMsgConstant.MSG_KEY_CONTENT, content);
        bundle.putString(SnapMsgConstant.MSG_KEY_COVER, cover);
        bundle.putString(SnapMsgConstant.MSG_KEY_SMART, smart);
        bundle.putString(SnapMsgConstant.MSG_KEY_UID, uid);
        bundle.putString(SnapMsgConstant.MSG_KEY_TIME, intime);
        bundle.putString(SnapMsgConstant.MSG_KEY_VIEWER, viewer);
        bundle.putInt(SnapMsgConstant.MSG_KEY_TYPE, type);
        bundle.putInt(SnapMsgConstant.MSG_KEY_READ, read);
        bundle.putInt(SnapMsgConstant.MSG_KEY_REPORT, report);
        bundle.putInt(SnapMsgConstant.MSG_KEY_COMMENT, comment);
        bundle.putInt(SnapMsgConstant.MSG_KEY_LIKE, like);
        bundle.putInt(SnapMsgConstant.MSG_KEY_STATUS, status);
        bundle.putInt("msgSts", msgStatus.getValue());
        bundle.putInt("attachSts", attachStatus.getValue());
        bundle.putInt("direction", directionEnum.getValue());
        bundle.putInt("msgType", messageType);

        return bundle;
    }

    public PublicSnapMessage (String json) {

        JSONObject jsonObject = JSON.parseObject(json);
        this.id = jsonObject.getString(SnapMsgConstant.MSG_KEY_ID);
        this.lat = jsonObject.getString(SnapMsgConstant.MSG_KEY_LAT);
        this.lng = jsonObject.getString(SnapMsgConstant.MSG_KEY_LNG);
        this.content = jsonObject.getString(SnapMsgConstant.MSG_KEY_CONTENT);
        this.cover = jsonObject.getString(SnapMsgConstant.MSG_KEY_COVER);
        this.smart = jsonObject.getString(SnapMsgConstant.MSG_KEY_SMART);
        this.uid = jsonObject.getString(SnapMsgConstant.MSG_KEY_UID);
        this.intime = jsonObject.getString(SnapMsgConstant.MSG_KEY_TIME);
        this.viewer = jsonObject.getString(SnapMsgConstant.MSG_KEY_VIEWER);

        this.type = jsonObject.getIntValue(SnapMsgConstant.MSG_KEY_TYPE);
        this.read = jsonObject.getIntValue(SnapMsgConstant.MSG_KEY_READ);
        this.report = jsonObject.getIntValue(SnapMsgConstant.MSG_KEY_REPORT);
        this.comment = jsonObject.getIntValue(SnapMsgConstant.MSG_KEY_COMMENT);
        this.like = jsonObject.getIntValue(SnapMsgConstant.MSG_KEY_LIKE);
        this.status = jsonObject.getIntValue(SnapMsgConstant.MSG_KEY_STATUS);
    }

    public PublicSnapMessage (PublishMessage publishMessage) {
        this.cover = publishMessage.getCover();
        this.coverLocalPath = publishMessage.getLocalPath();
        this.smart = publishMessage.getSmart();
        this.content = publishMessage.getContent();
        this.lat = publishMessage.getLat();
        this.lng = publishMessage.getLng();
        this.intime = TimeUtil.getBeijingNowTime("yyyy-MM-dd HH:mm");
        this.type = publishMessage.getType().value();

        this.comment = 0;
        this.like = 0;
        this.report = 0;
        this.read = 0;
        this.status = 1;
    }

    public void reset(PublicSnapMessage message) {
        this.id = message.getId();
        this.uid = message.getUid();
        this.lat = message.getLat();
        this.lng = message.getLng();
        this.content = message.getContent();
        this.cover = message.getCover();
        this.smart = message.getSmart();
        this.type = message.getType();
        this.intime = message.getIntime();
        this.viewer = message.getViewer();
        this.comment = message.getComment();
        this.like = message.getLike();
        this.report = message.getReport();
        this.read = message.getRead();
        this.status = message.getStatus();
        this.msgStatus = message.getMsgStatus();
        this.attachStatus = message.getAttachStatus();
        this.likeMessage = message.getLikeMessage();
    }

    public String getCoverLocalPath() {
        return coverLocalPath;
    }

    public void setCoverLocalPath(String coverLocalPath) {
        this.coverLocalPath = coverLocalPath;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public boolean isTheSame(PublicSnapMessage snapMessage){
        return TextUtils.equals(snapMessage.getId(), getId());
    }

    public LikeMessage getLikeMessage() {
        return likeMessage;
    }

    public void setLikeMessage(LikeMessage likeMessage) {
        this.likeMessage = likeMessage;
        if (likeMessage.getUids()!=null) {
            this.like = likeMessage.getUids().size();
        }
    }

    public void addLike(String uid) {
        if (likeMessage != null) {
            if (likeMessage.addLike(uid)) {
                like ++;
            }
        }
    }

    public void setViewer(String viewer) {
        this.viewer = viewer;
    }

    public String getViewer() {
        return viewer;
    }

    public void setDirection(MsgDirectionEnum directionEnum) {
        this.directionEnum = directionEnum;
    }

    public MsgDirectionEnum getDirect() {
        return directionEnum;
    }

    public void setMsgStatus(MsgStatusEnum msgStatus){
        this.msgStatus = msgStatus;
    }

    public MsgStatusEnum getMsgStatus(){
        return msgStatus;
    }

    public void setAttachStatus(AttachStatusEnum attachStatus) {
        this.attachStatus = attachStatus;
    }

    public AttachStatusEnum getAttachStatus() {
        return attachStatus;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public int getReport() {
        return report;
    }

    public void setReport(int report) {
        this.report = report;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    public String getIntime() {
        return intime;
    }

    public void setIntime(String intime) {
        this.intime = intime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSmart() {
        return smart;
    }

    public void setSmart(String smart) {
        this.smart = smart;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PublicSnapMessage)) return false;

        PublicSnapMessage that = (PublicSnapMessage) o;

        return getId().equals(that.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "PublicSnapMessage{" +
                "id=" + id +
                " uid=" + uid +
                " lat=" + lat +
                " lng=" + lng +
                " content=" + content +
                " cover=" + cover +
                " smart=" + smart +
                " type=" + type +
                " time=" + intime +
                " comment=" + comment +
                " zan=" + like +
                " report=" + report +
                " read=" + read +
                " status=" + status +
                '}';
    }
}
