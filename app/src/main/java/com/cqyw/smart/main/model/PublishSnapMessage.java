package com.cqyw.smart.main.model;

import android.os.Bundle;

import com.alibaba.fastjson.JSONObject;
import com.cqyw.smart.friend.model.Extras;

import java.io.Serializable;

/**
 * Created by Kairong on 2015/11/9.
 * mail:wangkrhust@gmail.com
 */
public class PublishSnapMessage implements Serializable{
    /*新鲜事的经度*/
    private String lat;
    /*新鲜事的纬度*/
    private String lng;
    /*文字内容*/
    private String content;
    /*封面url*/
    private String cover;
    /*smart图url*/
    private String smart;
    /*发送状态*/
    private int status;// 1 表示发送成功； 0 表示未成功。

    public PublishSnapMessage(){}

    public PublishSnapMessage(String lat, String lng, String content,
                              String cover, String smart, int status){
        this.lat = lat;
        this.lng = lng;
        this.content = content;
        this.cover = cover;
        this.smart = smart;
        this.status = status;
    }

    public PublishSnapMessage(Bundle bundle){
        this.lat = bundle.getString(SnapMsgConstant.MSG_KEY_LAT);
        this.lng = bundle.getString(SnapMsgConstant.MSG_KEY_LNG);
        this.content = bundle.getString(SnapMsgConstant.MSG_KEY_CONTENT);
        this.cover = bundle.getString(SnapMsgConstant.MSG_KEY_COVER);
        this.smart = bundle.getString(SnapMsgConstant.MSG_KEY_SMART);
        this.status = bundle.getInt(SnapMsgConstant.MSG_KEY_STATUS);
    }

    public PublishSnapMessage(PublicSnapMessage publicSnapMessage) {
        this.lat = publicSnapMessage.getLat();
        this.lng = publicSnapMessage.getLng();
        this.content = publicSnapMessage.getContent();
        this.cover = publicSnapMessage.getCover();
        this.smart = publicSnapMessage.getSmart();
        this.status = 0;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getSmart() {
        return smart;
    }

    public void setSmart(String smart) {
        this.smart = smart;
    }

    @Override
    public String toString() {
        return "PublishSnapMessage{" +
                " lat=" + lat +
                " lng=" + lng +
                " content=" + content +
                " cover=" + cover +
                " smart=" + smart +
                '}';
    }

    public JSONObject toJson(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(SnapMsgConstant.MSG_KEY_LAT, lat);
        jsonObject.put(SnapMsgConstant.MSG_KEY_LNG, lng);
        jsonObject.put(SnapMsgConstant.MSG_KEY_CONTENT, content);
        jsonObject.put(SnapMsgConstant.MSG_KEY_COVER, cover);
        jsonObject.put(SnapMsgConstant.MSG_KEY_SMART, smart);

        return jsonObject;
    }
}
