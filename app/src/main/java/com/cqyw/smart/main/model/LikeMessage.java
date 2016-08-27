package com.cqyw.smart.main.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 点赞信息
 * Created by Kairong on 2015/11/17.
 * mail:wangkrhust@gmail.com
 */
public class LikeMessage implements Serializable{
    /*新鲜事id*/
    private String nid;
    /*状态*/
    private String status;
    /*赞的uid*/
    private List<String> uids;

    public LikeMessage(){
        uids = new ArrayList<>();
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getNid() {
        return nid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getUids() {
        return uids;
    }

    public void setUids(List<String> uids) {
        if (this.uids == null) {
            this.uids = uids;
        } else if (uids.size() > this.uids.size()) {
            this.uids.clear();
            this.uids.addAll(uids);
        }
    }

    // 翻转uids
    public void reverseUids() {
        if (uids != null) {
            Collections.reverse(uids);
        }
    }

    public boolean addLike(String uid) {
//        List<String> newUids = new ArrayList<>(uids.size() + 1);
//        // 将自己最新的点赞放到最开始
//        newUids.add(uid);
//        if (uids.size() > 0) {
//            newUids.addAll(uids);
//        }
//        uids.clear();
//        uids.addAll(newUids);
        uids.add(0, uid);
        return true;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("LikeMessage{");
        sb.append("nid='").append(nid).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", uids=").append(uids==null?"[]":uids.toString());
        sb.append('}');
        return sb.toString();
    }
}
