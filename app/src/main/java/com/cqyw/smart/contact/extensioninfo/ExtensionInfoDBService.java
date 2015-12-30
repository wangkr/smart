package com.cqyw.smart.contact.extensioninfo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cqyw.smart.contact.extensioninfo.model.ExtensionInfo;
import com.cqyw.smart.main.database.AccountDBOpenHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kairong on 2015/12/25.
 * mail:wangkrhust@gmail.com
 */
public class ExtensionInfoDBService {
    AccountDBOpenHelper accountDBOpenHelper;
    public ExtensionInfoDBService (Context context) {
        accountDBOpenHelper = new AccountDBOpenHelper(context);
    }

    public void saveExtensionInfo(ExtensionInfo extensionInfo) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.execSQL("replace into extension_userinfo (id, jo_value, snap_value, photos) values (?, ?, ?, ?)", new Object[]{extensionInfo.getId(),
                    extensionInfo.getJo(), extensionInfo.getNewsno(), extensionInfo.getPhotos()});
        db.close();
    }

    public void saveExtensionInfoList(Map<String, ExtensionInfo> extensionInfoMap) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (String id : extensionInfoMap.keySet()) {
                ExtensionInfo info = extensionInfoMap.get(id);
                db.execSQL("replace into extension_userinfo (id, jo_value, snap_value, photos) values (?, ?, ?, ?)", new Object[]{info.getId(),
                        info.getJo(), info.getNewsno(), info.getPhotos()});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public Map<String, ExtensionInfo> getAllExtensionInfo() {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        Map<String, ExtensionInfo> userInfomap = new HashMap<>();

        Cursor cursor = db.rawQuery("select * from extension_userinfo", null);
        while (cursor.moveToNext()) {
            ExtensionInfo info = new ExtensionInfo();
            info.setId(cursor.getString(cursor.getColumnIndex("id")));
            info.setJo(cursor.getString(cursor.getColumnIndex("jo_value")));
            info.setNewsno(cursor.getString(cursor.getColumnIndex("snap_value")));
            info.setPhotos(cursor.getString(cursor.getColumnIndex("photos")));

            userInfomap.put(info.getId(), info);
        }

        cursor.close();
        db.close();

        return userInfomap;
    }
}
