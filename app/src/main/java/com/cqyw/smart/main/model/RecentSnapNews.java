package com.cqyw.smart.main.model;


import android.os.Bundle;

/**
 * 最近消息
 * Created by Kairong on 2015/11/15.
 * mail:wangkrhust@gmail.com
 */
public class RecentSnapNews{

    /*最近消息id*/
    private int id;
    /*新鲜事id*/
    private String nid;
    /*新鲜事封面*/
    private String cover;
    /*用户id*/
    private String uid;
    /*消息内容*/
    private String content;
    /*消息类型*/
    private SnapNewTypeEnum type;
    /*收到消息的时间*/
    private String intime;

    public RecentSnapNews(){}

    public RecentSnapNews(Bundle bundle) {
        if (bundle == null) {
            return;
        }

        this.id = bundle.getInt("id");
        this.nid = bundle.getString("nid");
        this.uid = bundle.getString("uid");
        this.content = bundle.getString("content");
        this.cover = bundle.getString("cover");
        this.type = SnapNewTypeEnum.getType(bundle.getInt("type"));
        this.intime = bundle.getString("intime");
    }

    public Bundle getSaveBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putString("nid", nid);
        bundle.putString("uid", uid);
        bundle.putString("content", content);
        bundle.putString("cover", cover);
        bundle.putInt("type", type.getValue());
        bundle.putString("intime", intime);
        return bundle;
    }


    public boolean isTheSame(RecentSnapNews snapNews){
        return getId() == snapNews.getId();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getCover() {
        return cover;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setType(int type) {
        this.type = SnapNewTypeEnum.getType(type);
    }

    public int getId() {
        return id;
    }

    public String getNid() {
        return nid;
    }

    public String getUid() {
        return uid;
    }

    public String getContent() {
        return content;
    }

    public SnapNewTypeEnum getType() {
        return type;
    }

    public void setIntime(String intime) {
        this.intime = intime;
    }

    public String getIntime() {
        return intime;
    }
}
