package com.cqyw.smart.contact;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by Kairong on 2015/10/15.
 * mail:wangkrhust@gmail.com
 */
public class ExtensionParse {
    /**
     *扩展字段正则字符串
     */
    private final static String UNIVERSITY_KEY = "university";
    private final static String EDUCATION_KEY = "grade";

    private ExtensionParse(){}

    public static ExtensionParse getInstance() {
        return InstanceHolder.instance;
    }


    public String getUniversity(String extension){
        if (TextUtils.isEmpty(extension)) {
            return "";
        }

        JSONObject jsonObject = JSONObject.parseObject(extension);
        return jsonObject.getString(UNIVERSITY_KEY);
    }
    public String getEducation(String extension){
        if (TextUtils.isEmpty(extension)) {
            return "";
        }

        JSONObject jsonObject = JSONObject.parseObject(extension);
        return jsonObject.getString(EDUCATION_KEY);
    }

    static class InstanceHolder{
        private static ExtensionParse instance = new ExtensionParse();
    }

}
