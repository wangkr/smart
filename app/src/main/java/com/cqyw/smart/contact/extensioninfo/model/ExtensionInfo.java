package com.cqyw.smart.contact.extensioninfo.model;

import java.io.Serializable;

/**
 * Created by Kairong on 2015/12/25.
 * mail:wangkrhust@gmail.com
 */
public class ExtensionInfo implements Serializable{
    /*用户id*/
    private String id;
    /*Jo值*/
    private String jo;
    /*自曝值*/
    private String newsno;
    /*相册*/
    private String photos;

    public ExtensionInfo(){}

    public ExtensionInfo(ExtensionInfoBean bean, String id) {
        this.id = id;
        this.jo = bean.getJo();
        this.photos = bean.getPhotos();
        this.newsno = bean.getNewsno();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getJo() {
        return jo;
    }

    public void setJo(String jo) {
        this.jo = jo;
    }

    public String getNewsno() {
        return newsno;
    }

    public void setNewsno(String newsno) {
        this.newsno = newsno;
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ExtensionInfo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", jo='").append(jo).append('\'');
        sb.append(", newsno='").append(newsno).append('\'');
        sb.append(", photos='").append(photos).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
