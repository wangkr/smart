package com.cqyw.smart.contact.extensioninfo.model;

import java.io.Serializable;

/**
 * Created by Kairong on 2015/12/27.
 * mail:wangkrhust@gmail.com
 */
public class ExtensionInfoBean implements Serializable {
    /*Jo值*/
    private String jo;
    /*自曝值*/
    private String newsno;
    /*相册*/
    private String photos;

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
}
