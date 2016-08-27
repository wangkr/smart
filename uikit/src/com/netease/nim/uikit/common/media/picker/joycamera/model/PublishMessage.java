package com.netease.nim.uikit.common.media.picker.joycamera.model;

import java.io.Serializable;

/**
 * Created by Kairong on 2015/11/9.
 * mail:wangkrhust@gmail.com
 */
public class PublishMessage implements Serializable{
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
    /*cover本地封面*/
    private String localPath;
    /*类型*/
    private MessageType type;
    /*发送状态*/
    private int status;// 1 表示发送成功； 0 表示未成功。

    public enum MessageType{
        SYSTEM(1),
        SNAP(2),
        PUB(4);
        private int value;
        MessageType(int value){
            this.value = value;
        }

        public int value(){return value;}
    }

    public PublishMessage(){}

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
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
        final StringBuffer sb = new StringBuffer("PublishSnapMessage{");
        sb.append("lat='").append(lat).append('\'');
        sb.append(", lng='").append(lng).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", cover='").append(cover).append('\'');
        sb.append(", smart='").append(smart).append('\'');
        sb.append(", localPath='").append(localPath).append('\'');
        sb.append(", type=").append(type);
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
