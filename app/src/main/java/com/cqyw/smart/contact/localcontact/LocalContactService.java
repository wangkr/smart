package com.cqyw.smart.contact.localcontact;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cqyw.smart.contact.helper.LocalContactDBOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kairong on 2015/11/28.
 * mail:wangkrhust@gmail.com
 */
public class LocalContactService {
    private LocalContactDBOpenHelper openHelper;
    public LocalContactService (Context context) {
        openHelper = new LocalContactDBOpenHelper(context);
    }

    public void saveLocalContactList(List<LocalContact> contactList) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (LocalContact contact : contactList) {
                db.execSQL("insert into local_contact (uid, phone, contact_name) values(?, ?, ?)", new Object[]{contact.getId(),
                contact.getPhone(), contact.getContactname()});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public List<LocalContact> findAll() {
        List<LocalContact> localContacts = new ArrayList<>();
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from local_contact order by id desc", null);
        while (cursor.moveToNext()) {
            LocalContact contact = new LocalContact();
            contact.setId(cursor.getString(cursor.getColumnIndex("uid")));
            contact.setPhone(cursor.getString(cursor.getColumnIndex("phone")));
            contact.setContactname(cursor.getString(cursor.getColumnIndex("contact_name")));
            localContacts.add(contact);
        }
        cursor.close();
        db.close();
        return localContacts;
    }

    public void deleteAll() {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        db.execSQL("delete from local_contact");
        db.close();
    }
}
