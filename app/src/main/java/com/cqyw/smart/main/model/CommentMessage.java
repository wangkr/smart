package com.cqyw.smart.main.model;

import android.text.TextUtils;

import com.cqyw.smart.main.viewholder.CommentMsgViewHolder;

/**
 * Created by Kairong on 2015/11/15.
 * mail:wangkrhust@gmail.com
 */
public class CommentMessage{
    // meta data
    /*评论id*/
    private String id;
    /*评论的新鲜事id*/
    private String nid;
    /*新鲜事发布者的uid*/
    private String nuid;
    /*评论(回复）人id*/
    private String uid;
    /*文字内容*/
    private String content;
    /*发布的时间*/
    private String intime;
    /*被回复的人*/
    private String ated; // added in v 1.0.4

    public boolean isTheSame(CommentMessage message) {
        if (message == null) {
            return false;
        } else if (TextUtils.equals(message.getId(), getId())) {
            return true;
        }

        return false;
    }

    public void setAted(String ated) {
        this.ated = ated;
    }

    public String getAted() {
        return ated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNuid(String nuid) {
        this.nuid = nuid;
    }

    public String getNuid() {
        return nuid;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getIntime() {
        return intime;
    }

    public void setIntime(String intime) {
        this.intime = intime;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CommentMessage{");
        sb.append("id='").append(id).append('\'');
        sb.append(", nid='").append(nid).append('\'');
        sb.append(", uid='").append(uid).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", intime='").append(intime).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
