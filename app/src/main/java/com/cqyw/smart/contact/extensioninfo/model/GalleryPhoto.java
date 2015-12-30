package com.cqyw.smart.contact.extensioninfo.model;

import java.io.Serializable;

/**
 * Created by Kairong on 2015/12/25.
 * mail:wangkrhust@gmail.com
 */
public class GalleryPhoto implements Serializable {
    private int index;
    private String url;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
