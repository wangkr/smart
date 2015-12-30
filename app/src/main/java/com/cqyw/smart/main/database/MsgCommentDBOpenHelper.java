package com.cqyw.smart.main.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Kairong on 2015/11/13.
 * mail:wangkrhust@gmail.com
 */
public class MsgCommentDBOpenHelper extends SQLiteOpenHelper {
    public static final String db_name = "public_snap_comment.db";
    public MsgCommentDBOpenHelper(Context context){
        super(context, db_name, null, 2);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE public_snap_comment ( id INTEGER PRIMARY KEY, nid text, uid text, content text, intime text, ated text ) ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("ALTER TABLE public_snap_comment rename to _temp_public_snap_comment");
            db.execSQL("CREATE TABLE public_snap_comment ( id INTEGER PRIMARY KEY, nid text, uid text, content text, intime text, ated text ) ");
            db.execSQL("INSERT INTO public_snap_comment select *, ' ' from _temp_public_snap_comment");
            db.execSQL("DROP TABLE _temp_public_snap_comment");
        }
    }
}
