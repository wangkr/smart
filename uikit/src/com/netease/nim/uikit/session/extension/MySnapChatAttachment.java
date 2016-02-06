package com.netease.nim.uikit.session.extension;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.session.extension.CustomAttachParser;
import com.netease.nim.uikit.session.extension.CustomAttachmentType;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;


/**
 * Created by Kairong on 2015/11/7.
 * mail:wangkrhust@gmail.com
 */
public class MySnapChatAttachment extends FileAttachment {
    private static final String KEY_SIZE = "size";
    private static final String KEY_MD5 = "md5";
    private static final String KEY_URL = "url";
    private static final String KEY_EXT = "ext";// 封面图片url

    public MySnapChatAttachment(){
        super();
    }

    public MySnapChatAttachment(JSONObject jsonObject){
        load(jsonObject);
    }

    @Override
    public String toJson(boolean send) {
        JSONObject data = new JSONObject();
        try {
            if (!TextUtils.isEmpty(md5)) {
                data.put(KEY_MD5, md5);
            }
            data.put(KEY_URL, url);
            data.put(KEY_SIZE, size);
            data.put(KEY_EXT, extension);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CustomAttachParser.packData(CustomAttachmentType.SnapChat, data);
    }

    private void load(JSONObject data){
        md5 = data.getString(KEY_MD5);
        url = data.getString(KEY_URL);
        extension = data.getString(KEY_EXT);
    }

    @Override
    public String getThumbPath() {
        return super.getThumbPath();
    }
}
