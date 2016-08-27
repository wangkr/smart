package com.cqyw.smart.main.service;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cqyw.smart.main.database.AccountDBOpenHelper;
import com.netease.nim.uikit.common.media.picker.joycamera.model.CamOnLineRes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Kairong on 2016/8/7.
 * mail:wangkrhust@gmail.com
 */
public class OnlineResDBService {
    private AccountDBOpenHelper accountDBOpenHelper;
    public OnlineResDBService(Context context) {
        accountDBOpenHelper = new AccountDBOpenHelper(context);
    }

    public static final String seperator = "|";

    public static String makeString(List<String> strings){
        StringBuilder sb = new StringBuilder();
        if (strings==null || strings.size() == 0) return "";
        for(String s : strings) {
            sb.append(s).append(seperator);
        }

        if (sb.length() > 0){
            sb.deleteCharAt(sb.length()-1);
        }

        return sb.toString();
    }

    public static List<String> splitString(String s){
        List<String> stringList = new ArrayList<>();
        if (!TextUtils.isEmpty(s)){
            String[] strings = s.split(seperator);
            stringList.addAll(Arrays.asList(strings));
        }

        return stringList;
    }

    public void saveSingleRes(CamOnLineRes res) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.execSQL("replace into joycamera_onlineres(_id, url, icon_url, cache_path, icon_cache_path, param_cache_paths, _status, _type, local_index) values" +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{res.getId(), res.getUrl(), res.getIconUrl(),
                res.getCachePath(), res.getIconCachePath(), makeString(res.getParamCachePaths()), res.getStatus().name(), res.getType().name(), res.getLocalIndex()});
        db.close();
    }

    public void saveAllOnlineRes(List<CamOnLineRes> camOnLineRes){
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (CamOnLineRes res : camOnLineRes) {
                db.execSQL("replace into joycamera_onlineres(_id, url, icon_url, cache_path, icon_cache_path, param_cache_paths, _status, _type, local_index) values" +
                        "(?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{res.getId(), res.getUrl(), res.getIconUrl(),
                res.getCachePath(), res.getIconCachePath(), makeString(res.getParamCachePaths()), res.getStatus().name(), res.getType().name(), res.getLocalIndex()});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public List<CamOnLineRes> findAllOnlineRes(HashSet<Integer> localIds,CamOnLineRes.Type type){
        SQLiteDatabase db = accountDBOpenHelper.getReadableDatabase();
        List<CamOnLineRes> resList = new ArrayList<>();

        Cursor cursor = db.rawQuery("select * from joycamera_onlineres where _type=? order by _id ASC", new String[]{type.name()});

        while (cursor.moveToNext()){
            CamOnLineRes colr = new CamOnLineRes();
            colr.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            colr.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            colr.setIconUrl(cursor.getString(cursor.getColumnIndex("icon_url")));
            colr.setCachePath(cursor.getString(cursor.getColumnIndex("cache_path")));
            colr.setIconCachePath(cursor.getString(cursor.getColumnIndex("icon_cache_path")));
            colr.setParamCachePaths(splitString(cursor.getString(cursor.getColumnIndex("param_cache_paths"))));
            colr.setStatus(CamOnLineRes.Status.valueOf(cursor.getString(cursor.getColumnIndex("_status"))));
            colr.setType(CamOnLineRes.Type.valueOf(cursor.getString(cursor.getColumnIndex("_type"))));
            colr.setLocalIndex(cursor.getInt(cursor.getColumnIndex("local_index")));

            localIds.add(colr.getId());
            resList.add(colr);
        }

        cursor.close();
        db.close();

        return resList;
    }
}
