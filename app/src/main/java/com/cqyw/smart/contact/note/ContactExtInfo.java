package com.cqyw.smart.contact.note;

import java.io.Serializable;

/**
 * Created by Kairong on 2015/12/28.
 * mail:wangkrhust@gmail.com
 */
public class ContactExtInfo {
    private String friends_id;
    private String note;
    private String nick;
    private String jo;

    public ContactExtInfo() {}

    public String getFriends_id() {
        return friends_id;
    }

    public void setFriends_id(String friends_id) {
        this.friends_id = friends_id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getJo() {
        return jo;
    }

    public void setJo(String jo) {
        this.jo = jo;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ContactExtInfo{");
        sb.append("friends_id='").append(friends_id).append('\'');
        sb.append(", note='").append(note).append('\'');
        sb.append(", nick='").append(nick).append('\'');
        sb.append(", jo='").append(jo).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
