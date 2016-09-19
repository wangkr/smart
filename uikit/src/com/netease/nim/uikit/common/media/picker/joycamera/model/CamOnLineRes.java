package com.netease.nim.uikit.common.media.picker.joycamera.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Kairong on 2016/8/7.
 * mail:wangkrhust@gmail.com
 */
public class CamOnLineRes implements Serializable {
    /**
     * 资源id
     */
    private int id;

    /**
     * 资源url
     */
    private String url;

    /**
     * 资源icon url
     */
    private String iconUrl;

    /**
     * 资源本地路径
     */
    private String cachePath;

    /**
     * 资源icon 本地路径
     */
    private String iconCachePath;

    /**
     * 资源参数本地路径
     */
    private List<String> paramCachePaths;

    /**
     * 资源状态
     */
    private Status status = Status.NONE;

    /**
     * 本地顺序
     */
    private int localIndex;

    /**
     * 类型
     */
    private Type type;

    public enum Type{
        COVER(0x01),
        AR(0x10);
        private int v;
        Type(int v){
            this.v = v;
        }
        public int v() {return v;}
    }

    public enum Status{
        NONE(0x0000),       /*本地没有资源，但是已经有url了*/
        DOWNLOADING(0x0001),/*正在下载*/
        PART(0x0010),       /*主资源和缩略图只有一个下载完成，没有下载完成的路径为空*/
        COMPLETE(0x0100),   /*主资源和缩略图都下载完成且解压*/
        DEFAULT(0x1000);    /*本地默认资源*/
        private int value;
        Status(int value){
            this.value = value;
        }
        public int value(){return value;}
    }

    public CamOnLineRes(){

    }

    public CamOnLineRes(int id, String url, String iconUrl, String cachePath,
                        String iconCachePath, List<String> paramCachePaths, Status status, Type type, int localIndex) {
        this.id = id;
        this.url = url;
        this.iconUrl = iconUrl;
        this.cachePath = cachePath;
        this.iconCachePath = iconCachePath;
        this.paramCachePaths = paramCachePaths;
        this.status = status;
        this.type = type;
        this.localIndex = localIndex;
    }

    public void update(CamOnLineRes res) {
        this.url = res.getUrl();
        this.iconUrl = res.getIconUrl();
        this.cachePath = res.getCachePath();
        this.iconCachePath = res.getIconCachePath();
//        this.paramCachePaths = res.getParamCachePaths();
        this.status = res.getStatus();
        this.type = res.getType();
        this.localIndex = res.getLocalIndex();
    }

    public int getLocalIndex() {
        return localIndex;
    }

    public void setLocalIndex(int localIndex) {
        this.localIndex = localIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CamOnLineRes)) return false;

        CamOnLineRes that = (CamOnLineRes) o;

        return getId() == that.getId();

    }

    @Override
    public int hashCode() {
        return getId();
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getIconCachePath() {
        return iconCachePath;
    }

    public void setIconCachePath(String iconCachePath) {
        this.iconCachePath = iconCachePath;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getCachePath() {
        return cachePath;
    }

    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
    }

    public List<String> getParamCachePaths() {
        return paramCachePaths;
    }

    public void setParamCachePaths(List<String> paramCachePaths) {
        this.paramCachePaths = paramCachePaths;
    }
}
