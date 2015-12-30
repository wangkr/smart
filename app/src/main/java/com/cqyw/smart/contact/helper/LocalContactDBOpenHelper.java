package com.cqyw.smart.contact.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cqyw.smart.config.AppCache;

/**
 * Created by Kairong on 2015/11/28.
 * mail:wangkrhust@gmail.com
 */
public class LocalContactDBOpenHelper extends SQLiteOpenHelper {
    private static String db_name = AppCache.getJoyId()+"_local_contact.db";
    public LocalContactDBOpenHelper (Context context) {
        super(context, db_name, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE local_contact (id INTEGER PRIMARY KEY AUTOINCREMENT, uid, text, phone text, contact_name text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
