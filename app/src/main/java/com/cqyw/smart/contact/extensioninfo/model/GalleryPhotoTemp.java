package com.cqyw.smart.contact.extensioninfo.model;

import java.io.Serializable;

/**
 * Created by Kairong on 2015/12/26.
 * mail:wangkrhust@gmail.com
 */
public class GalleryPhotoTemp implements Serializable {
    private int index;
    private String url;
    private String path;
    private String thumbPath;
    private boolean isLocal;
    private boolean isShownMenu;
    private boolean isAddBtn;

    public GalleryPhotoTemp() {
        isLocal = false;
        isShownMenu = false;
        isAddBtn = false;
    }

    public GalleryPhotoTemp (GalleryPhoto galleryPhoto) {
        this.index = galleryPhoto.getIndex();
        this.url = galleryPhoto.getUrl();
        isLocal = false;
        isShownMenu = false;
        isAddBtn = false;
    }

    public void setAddBtn(boolean addBtn) {
        isAddBtn = addBtn;
    }

    public boolean isAddBtn() {
        return isAddBtn;
    }

    public void setShownMenu(boolean shownMenu) {
        isShownMenu = shownMenu;
    }

    public boolean isShownMenu() {
        return isShownMenu;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }
}
