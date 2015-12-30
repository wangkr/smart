package com.cqyw.smart.main.service;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cqyw.smart.main.database.AccountDBOpenHelper;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.cqyw.smart.main.model.SnapMsgConstant;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Kairong on 2015/11/17.
 * mail:wangkrhust@gmail.com
 */
public class PublicSnapMessageDBService {
    private AccountDBOpenHelper accountDBOpenHelper;
    public PublicSnapMessageDBService(Context context) {
        accountDBOpenHelper = new AccountDBOpenHelper(context);
    }

    public void savePublicSnapMessage(PublicSnapMessage message) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.execSQL("replace into snap_message( id, uid, lat, lng, content, cover, smart, type, " +
                "intime, comment, like, report, read, status, msgsts, attachsts, direction, msgtype, viewer) " +
                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{message.getId(), message.getUid(), message.getLat(),
                message.getLng(), message.getContent(), message.getCover(), message.getSmart(), message.getType(), message.getIntime(),
                message.getComment(), message.getLike(), message.getReport(), message.getRead(), message.getStatus(),
                message.getMsgStatus().getValue(), message.getAttachStatus().getValue(), message.getDirect().getValue(), message.getMessageType(),
                message.getViewer()});
        db.close();
    }

    public void savePublicSnapMessageList(List<PublicSnapMessage> messages) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (PublicSnapMessage message : messages) {
                db.execSQL("replace into snap_message( id, uid, lat, lng, content, cover, smart, type, " +
                        "intime, comment, like, report, read, status, msgsts, attachsts, direction, msgtype, viewer) " +
                        "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{message.getId(), message.getUid(), message.getLat(),
                        message.getLng(), message.getContent(), message.getCover(), message.getSmart(), message.getType(), message.getIntime(),
                        message.getComment(), message.getLike(), message.getReport(), message.getRead(), message.getStatus(),
                        message.getMsgStatus().getValue(), message.getAttachStatus().getValue(), message.getDirect().getValue(), message.getMessageType(),
                        message.getViewer()});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public void deletePublicSnapMessageByNid(String id) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.execSQL("delete from snap_message where id=?",
                new Object[] { Integer.valueOf(id) });
        db.close();
    }

    public void deletePublicSnapMessageByUid(String uid) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.execSQL("delete from snap_message where uid=?",
                new Object[] { uid });
        db.close();
    }

    public void deletePublicSnapMessageByMsg(PublicSnapMessage msg) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.execSQL(
                "delete from snap_message where (id=? and uid=?)",
                new Object[] { Integer.valueOf(msg.getId()), msg.getUid()});
        db.close();
    }

    public void deleteAllPublicSnapMessage() {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.execSQL("delete from snap_message");
        db.close();
    }

    public PublicSnapMessage findPublicSnapMessageById(String id) {
        LogUtil.e("PublicSnapMessageListDBService", "accountDBOpenHelper == null ? " + (accountDBOpenHelper == null));
        SQLiteDatabase db = accountDBOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select * from snap_message where id=? order by id",
                new String[] { id });
        PublicSnapMessage publicMessage = new PublicSnapMessage();
        if (cursor.moveToFirst()) {
            publicMessage.setId("" + cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_ID)));
            publicMessage.setUid(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_UID)));
            publicMessage.setContent(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_CONTENT)));
            publicMessage.setLat(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_LAT)));
            publicMessage.setLng(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_LNG)));
            publicMessage.setCover(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_COVER)));
            publicMessage.setSmart(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_SMART)));

            publicMessage.setRead(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_READ)));
            publicMessage.setStatus(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_STATUS)));
            publicMessage.setComment(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_COMMENT)));
            publicMessage.setType(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_TYPE)));
            publicMessage.setLike(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_LIKE)));
            publicMessage.setIntime(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_TIME)));
            publicMessage.setViewer(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_VIEWER)));
            publicMessage.setReport(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_REPORT)));

            publicMessage.setMsgStatus(MsgStatusEnum.statusOfValue(cursor.getInt(cursor.getColumnIndex("msgsts"))));
            publicMessage.setAttachStatus(AttachStatusEnum.statusOfValue(cursor.getInt(cursor.getColumnIndex("attachsts"))));
            publicMessage.setDirection(MsgDirectionEnum.directionOfValue(cursor.getInt(cursor.getColumnIndex("direction"))));
            publicMessage.setMessageType(cursor.getInt(cursor.getColumnIndex("msgtype")));
            cursor.close();
            db.close();
            return publicMessage;
        }
        db.close();
        return null;
    }

    public PublicSnapMessage findPublicSnapMessageByUid(String uid) {
        SQLiteDatabase db = accountDBOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select * from snap_message where uid=? order by id",
                new String[] { uid });
        PublicSnapMessage publicMessage = new PublicSnapMessage();
        if (cursor.moveToFirst()) {
            publicMessage.setId(""+cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_ID)));
            publicMessage.setUid(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_UID)));
            publicMessage.setContent(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_CONTENT)));
            publicMessage.setLat(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_LAT)));
            publicMessage.setLng(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_LNG)));
            publicMessage.setCover(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_COVER)));
            publicMessage.setSmart(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_SMART)));
            publicMessage.setRead(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_READ)));
            publicMessage.setStatus(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_STATUS)));
            publicMessage.setComment(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_COMMENT)));
            publicMessage.setType(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_TYPE)));
            publicMessage.setLike(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_LIKE)));
            publicMessage.setIntime(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_TIME)));
            publicMessage.setReport(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_REPORT)));
            publicMessage.setViewer(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_VIEWER)));

            publicMessage.setMsgStatus(MsgStatusEnum.statusOfValue(cursor.getInt(cursor.getColumnIndex("msgsts"))));
            publicMessage.setAttachStatus(AttachStatusEnum.statusOfValue(cursor.getInt(cursor.getColumnIndex("attachsts"))));
            publicMessage.setDirection(MsgDirectionEnum.directionOfValue(cursor.getInt(cursor.getColumnIndex("direction"))));
            publicMessage.setMessageType(cursor.getInt(cursor.getColumnIndex("msgtype")));
            cursor.close();
            db.close();
            return publicMessage;
        }
        db.close();
        return null;
    }

    public LinkedList<PublicSnapMessage> findAllPublicSnapMessage() {
        SQLiteDatabase db = accountDBOpenHelper.getReadableDatabase();
        LinkedList<PublicSnapMessage> msgs = new LinkedList<PublicSnapMessage>();
        Cursor cursor = db.rawQuery(
                "select * from snap_message order by id desc", null);
        while (cursor.moveToNext()) {
            PublicSnapMessage publicMessage = new PublicSnapMessage();
            publicMessage.setId(""+cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_ID)));
            publicMessage.setUid(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_UID)));
            publicMessage.setContent(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_CONTENT)));
            publicMessage.setLat(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_LAT)));
            publicMessage.setLng(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_LNG)));
            publicMessage.setCover(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_COVER)));
            publicMessage.setSmart(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_SMART)));
            publicMessage.setRead(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_READ)));
            publicMessage.setStatus(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_STATUS)));
            publicMessage.setComment(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_COMMENT)));
            publicMessage.setType(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_TYPE)));
            publicMessage.setLike(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_LIKE)));
            publicMessage.setIntime(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_TIME)));
            publicMessage.setReport(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_REPORT)));
            publicMessage.setViewer(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_VIEWER)));

            publicMessage.setMsgStatus(MsgStatusEnum.statusOfValue(cursor.getInt(cursor.getColumnIndex("msgsts"))));
            publicMessage.setAttachStatus(AttachStatusEnum.statusOfValue(cursor.getInt(cursor.getColumnIndex("attachsts"))));
            publicMessage.setDirection(MsgDirectionEnum.directionOfValue(cursor.getInt(cursor.getColumnIndex("direction"))));
            publicMessage.setMessageType(cursor.getInt(cursor.getColumnIndex("msgtype")));
            msgs.add(publicMessage);
        }
        cursor.close();
        db.close();
        return msgs;
    }

    public LinkedList<PublicSnapMessage> findLatestPublicSnapMessages(int items) {
        SQLiteDatabase db = accountDBOpenHelper.getReadableDatabase();
        LinkedList<PublicSnapMessage> msgs = new LinkedList<PublicSnapMessage>();
        Cursor cursor = db.rawQuery(
                "select * from snap_message order by id desc limit ?", new String[] {""+items});
        while (cursor.moveToNext()) {
            PublicSnapMessage publicMessage = new PublicSnapMessage();
            publicMessage.setId(""+cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_ID)));
            publicMessage.setUid(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_UID)));
            publicMessage.setContent(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_CONTENT)));
            publicMessage.setLat(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_LAT)));
            publicMessage.setLng(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_LNG)));
            publicMessage.setCover(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_COVER)));
            publicMessage.setSmart(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_SMART)));
            publicMessage.setRead(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_READ)));
            publicMessage.setStatus(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_STATUS)));
            publicMessage.setComment(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_COMMENT)));
            publicMessage.setType(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_TYPE)));
            publicMessage.setLike(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_LIKE)));
            publicMessage.setIntime(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_TIME)));
            publicMessage.setReport(cursor.getInt(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_REPORT)));
            publicMessage.setViewer(cursor.getString(cursor.getColumnIndex(SnapMsgConstant.MSG_KEY_VIEWER)));

            publicMessage.setMsgStatus(MsgStatusEnum.statusOfValue(cursor.getInt(cursor.getColumnIndex("msgsts"))));
            publicMessage.setAttachStatus(AttachStatusEnum.statusOfValue(cursor.getInt(cursor.getColumnIndex("attachsts"))));
            publicMessage.setDirection(MsgDirectionEnum.directionOfValue(cursor.getInt(cursor.getColumnIndex("direction"))));
            publicMessage.setMessageType(cursor.getInt(cursor.getColumnIndex("msgtype")));
            msgs.add(publicMessage);
        }
        cursor.close();
        db.close();
        return msgs;
    }

    public void updatePublicSnapMessage(PublicSnapMessage msg) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "select * from snap_message where id=?", new String[] { msg.getId() });
        if (cursor.moveToFirst()) {
            db.execSQL(
                    "update snap_message set like=?, read=?, status=? where id=?",
                    new Object[] { msg.getLike(), msg.getRead(),msg.getStatus(),
                            Integer.valueOf(msg.getId())});
        }
        cursor.close();
        db.close();
    }
}
