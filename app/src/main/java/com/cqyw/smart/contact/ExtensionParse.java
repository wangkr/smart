package com.cqyw.smart.contact;

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
    private static JSONObject jsonObject = null;
    public static void init(String extension){
        jsonObject = JSONObject.parseObject(extension);
    }
    public static String getUniversity(){
        if(jsonObject == null){
            return "";
        }
        return jsonObject.getString(UNIVERSITY_KEY);
    }
    public static String getEducation(){
        if(jsonObject == null){
            return "";
        }
        return jsonObject.getString(EDUCATION_KEY);
    }

}
