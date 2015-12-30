package com.cqyw.smart.main.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Kairong on 2015/11/13.
 * mail:wangkrhust@gmail.com
 */
public class MsgLikeDBOpenHelper extends SQLiteOpenHelper {

    public static final String db_name = "public_snap_like.db";
    public MsgLikeDBOpenHelper(Context context){
        super(context, db_name, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("CREATE TABLE public_snap_like ([id] INTEGER PRIMARY KEY AUTOINCREMENT, nid text, uid text ) ");
            db.execSQL("CREATE UNIQUE INDEX nid_uid_unique ON public_snap_like(nid, uid)");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
