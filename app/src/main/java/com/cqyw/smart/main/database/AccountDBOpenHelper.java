package com.cqyw.smart.main.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cqyw.smart.config.AppCache;
import com.netease.nim.uikit.common.util.string.MD5;

/**
 * Created by Kairong on 2015/11/17.
 * mail:wangkrhust@gmail.com
 */
public class AccountDBOpenHelper extends SQLiteOpenHelper{
    public AccountDBOpenHelper(Context context) {
        super(context, "cache" + AppCache.getJoyId()+".db", null, 6);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建数据库表
        db.execSQL("CREATE TABLE snap_message (id INTEGER PRIMARY KEY, uid TEXT, lat TEXT, lng TEXT, content varchar(140) NULL, cover TEXT," +
                " smart TEXT,[type] INTEGER, intime TEXT, comment INTEGER, [like] INTEGER, report INTEGER, [read] INTEGER, status INTEGER, " +
                " msgsts INTEGER, attachsts INTEGER, direction INTEGER, msgtype INTEGER, viewer TEXT)");
        db.execSQL("CREATE TABLE my_snap_message ( id INTEGER PRIMARY KEY AUTOINCREMENT, lat TEXT, lng TEXT, content varchar(140) NULL, cover TEXT,smart TEXT, status INTEGER,  _type INTEGER, local_path TEXT)");
        db.execSQL("CREATE TABLE recent_snapnews( id INTEGER PRIMARY KEY AUTOINCREMENT, uid TEXT NULL, nid TEXT NULL, content TEXT, cover TEXT, type INTEGER, intime TEXT)");
        db.execSQL("CREATE TABLE message_mark (nid TEXT PRIMARY KEY, uid TEXT)");
        // added db4
        db.execSQL("CREATE TABLE extension_userinfo (id TEXT PRIMARY KEY, jo_value TEXT, snap_value TEXT, photos TEXT)");
        // added db5
        db.execSQL("CREATE TABLE joycamera_onlineres (_id INTEGER PRIMARY KEY, url TEXT, icon_url TEXT, cache_path TEXT, " +
                "icon_cache_path TEXT, param_cache_paths TEXT, _status TEXT, _type TEXT, local_index INTEGER)");
        // added db6
        db.execSQL("CREATE TABLE onlineres_stat (_id INTEGER PRIMARY KEY, _times INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1: // added on db2
                db.execSQL("ALTER TABLE recent_snapnews rename to _temp_recent_snapnews");
                db.execSQL("CREATE TABLE recent_snapnews ( id INTEGER PRIMARY KEY AUTOINCREMENT, uid TEXT NULL, nid TEXT NULL, content TEXT, cover TEXT, type INTEGER, intime TEXT)");
                db.execSQL("INSERT INTO recent_snapnews select *, ' ' from _temp_recent_snapnews");
                db.execSQL("DROP TABLE _temp_recent_snapnews");
            case 2: // added on db3
                db.execSQL("ALTER TABLE message_mark rename to _temp_message_mark");
                db.execSQL("CREATE TABLE message_mark (nid TEXT PRIMARY KEY, uid TEXT)");
                db.execSQL("INSERT INTO message_mark select *, ' ' from _temp_message_mark");
                db.execSQL("DROP TABLE _temp_message_mark");
            case 3:// added on db4
                db.execSQL("ALTER TABLE snap_message rename to _temp_snap_message");
                db.execSQL("CREATE TABLE snap_message (id INTEGER PRIMARY KEY, uid TEXT, lat TEXT, lng TEXT, content varchar(140) NULL, cover TEXT," +
                        " smart TEXT,[type] INTEGER, intime TEXT, comment INTEGER, [like] INTEGER, report INTEGER, [read] INTEGER, status INTEGER, " +
                        " msgsts INTEGER, attachsts INTEGER, direction INTEGER, msgtype INTEGER, viewer TEXT)");
                db.execSQL("INSERT INTO snap_message select *, ' ' from _temp_snap_message");
                db.execSQL("DROP TABLE _temp_snap_message");

                db.execSQL("CREATE TABLE extension_userinfo (id TEXT PRIMARY KEY, jo_value TEXT, snap_value TEXT, photos TEXT)");
            case 4: // added on db5
                db.execSQL("ALTER TABLE my_snap_message rename to _temp_my_snap_message");
                db.execSQL("CREATE TABLE my_snap_message ( id INTEGER PRIMARY KEY AUTOINCREMENT, lat TEXT, lng TEXT, content varchar(140) NULL, cover TEXT,smart TEXT, status INTEGER,  _type INTEGER, local_path TEXT)");
                db.execSQL("INSERT INTO my_snap_message select *, ' ' from _temp_my_snap_message");
                db.execSQL("DROP TABLE _temp_my_snap_message");

                db.execSQL("CREATE TABLE joycamera_onlineres (_id INTEGER PRIMARY KEY, url TEXT, icon_url TEXT, cache_path TEXT, " +
                        "icon_cache_path TEXT, param_cache_paths TEXT, _status TEXT, _type TEXT,local_index INTEGER)");
            case 5:// added on db6
                db.execSQL("CREATE TABLE onlineres_stat(_id INTEGER PRIMARY KEY, _times INTEGER)");
                break;
        }

    }
}
