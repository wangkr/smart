package com.cqyw.smart.contact.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Kairong on 2015/10/30.
 * mail:wangkrhust@gmail.com
 */
public class FriendNoteDBOpenHelper extends SQLiteOpenHelper {
    public static final String db_name = "friend_note.db";
    public FriendNoteDBOpenHelper(Context context){
        super(context,db_name,null,2);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE note(friend_id TEXT PRIMARY KEY, friend_nick text, note text, university text, jo text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("ALTER TABLE note rename to _temp_note ");
        db.execSQL("CREATE TABLE note (friend_id TEXT PRIMARY KEY, friend_nick text, note text, university text, jo text)");
        db.execSQL("INSERT INTO note select * , ' ' from _temp_note");
        db.execSQL("DROP TABLE _temp_note");
    }
}
