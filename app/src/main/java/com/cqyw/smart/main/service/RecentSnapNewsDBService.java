package com.cqyw.smart.main.service;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cqyw.smart.main.database.AccountDBOpenHelper;
import com.cqyw.smart.main.model.RecentSnapNews;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Kairong on 2015/11/17.
 * mail:wangkrhust@gmail.com
 */
public class RecentSnapNewsDBService {
    private AccountDBOpenHelper accountDBOpenHelper;
    public RecentSnapNewsDBService(Context context) {
        accountDBOpenHelper = new AccountDBOpenHelper(context);
    }

    public int saveNewRecentSnapNews(RecentSnapNews message) {
        int id = 1;
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("insert into recent_snapnews(id, uid, nid, content, cover, type, intime) values(NULL, ?, ?, ?, ?, ?, ?)",
                    new Object[]{message.getUid(), message.getNid(), message.getContent(), message.getCover(), message.getType().getValue(), message.getIntime()});
            Cursor cursor = db.rawQuery("select max(id) as newid from recent_snapnews", null);
            if (cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndex("newid"));
            }
            db.setTransactionSuccessful();
            cursor.close();
        } finally {
            db.endTransaction();
            db.close();
        }

        return id;
    }

    public void saveNewRecentSnapNewsList(List<RecentSnapNews> messages) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (RecentSnapNews message : messages) {
                db.execSQL("insert into recent_snapnews(id, uid, nid, content, cover, type, intime) values(NULL, ?, ?, ?, ?, ?, ?)",
                        new Object[]{message.getId(), message.getUid(), message.getNid(), message.getContent(), message.getCover(), message.getType().getValue(), message.getIntime()});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public void saveRecentSnapNewsList(List<RecentSnapNews> messages) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (RecentSnapNews message : messages) {
                db.execSQL("insert into recent_snapnews(id, uid, nid, content, cover, type, intime) values(?, ?, ?, ?, ?, ?, ?)",
                        new Object[]{message.getId(), message.getUid(), message.getNid(), message.getContent(), message.getCover(), message.getType().getValue(), message.getIntime()});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public void deleteRecentSnapNewsById(int id) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.execSQL("delete from recent_snapnews where id=?",
                new Object[]{id});
        db.close();
    }

    public void deleteAllRecentSnapNews() {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.execSQL("delete from recent_snapnews");
        db.close();
    }

    public LinkedList<RecentSnapNews> findAllRecentSnapNews() {
        SQLiteDatabase db = accountDBOpenHelper.getReadableDatabase();
        LinkedList<RecentSnapNews> msgs = new LinkedList<RecentSnapNews>();
        Cursor cursor = db.rawQuery(
                "select * from recent_snapnews order by id desc", null);
        while (cursor.moveToNext()) {
            RecentSnapNews recentSnapNews = new RecentSnapNews();
            recentSnapNews.setId(cursor.getInt(cursor.getColumnIndex("id")));
            recentSnapNews.setUid(cursor.getString(cursor.getColumnIndex("uid")));
            recentSnapNews.setContent(cursor.getString(cursor.getColumnIndex("content")));
            recentSnapNews.setNid(cursor.getString(cursor.getColumnIndex("nid")));
            recentSnapNews.setType(cursor.getInt(cursor.getColumnIndex("type")));
            recentSnapNews.setCover(cursor.getString(cursor.getColumnIndex("cover")));
            recentSnapNews.setIntime(cursor.getString(cursor.getColumnIndex("intime")));
            msgs.add(recentSnapNews);
        }
        cursor.close();
        db.close();
        return msgs;
    }

    public LinkedList<RecentSnapNews> findAllRecentSnapNews(int newestNum) {
        SQLiteDatabase db = accountDBOpenHelper.getReadableDatabase();
        LinkedList<RecentSnapNews> msgs = new LinkedList<RecentSnapNews>();
        Cursor cursor = db.rawQuery(
                "select * from recent_snapnews order by id desc limit ?", new String[]{newestNum+""});
        while (cursor.moveToNext()) {
            RecentSnapNews recentSnapNews = new RecentSnapNews();
            recentSnapNews.setId(cursor.getInt(cursor.getColumnIndex("id")));
            recentSnapNews.setUid(cursor.getString(cursor.getColumnIndex("uid")));
            recentSnapNews.setContent(cursor.getString(cursor.getColumnIndex("content")));
            recentSnapNews.setNid(cursor.getString(cursor.getColumnIndex("nid")));
            recentSnapNews.setCover(cursor.getString(cursor.getColumnIndex("cover")));
            recentSnapNews.setType(cursor.getInt(cursor.getColumnIndex("type")));
            recentSnapNews.setIntime(cursor.getString(cursor.getColumnIndex("intime")));
            msgs.add(recentSnapNews);
        }
        cursor.close();
        db.close();
        return msgs;
    }
}
