package com.cqyw.smart.contact.localcontact;

import java.io.Serializable;

/**
 * 本地联系人
 * Created by Kairong on 2015/11/28.
 * mail:wangkrhust@gmail.com
 */
public class LocalContact implements Serializable{
    /*电话号码*/
    private String phone;
    /*通讯录姓名*/
    private String contactname;
    /*账号*/
    private String id;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContactname() {
        return contactname;
    }

    public void setContactname(String contactname) {
        this.contactname = contactname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
