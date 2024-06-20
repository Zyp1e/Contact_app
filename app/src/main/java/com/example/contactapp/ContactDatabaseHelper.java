package com.example.contactapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ContactDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "contacts.db";
    public static final String TABLE_NAME = "contacts";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_NICKNAME = "nickname";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_CONTACT_GROUP = "contact_group";
    public static final String COLUMN_PHOTO_URI = "photo_uri";

    public ContactDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME + " TEXT," +
                COLUMN_NICKNAME + " TEXT," +
                COLUMN_PHONE_NUMBER + " TEXT," +
                COLUMN_CONTACT_GROUP + " TEXT," +
                COLUMN_PHOTO_URI + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, contact.getName());
        values.put(COLUMN_NICKNAME, contact.getNickname());
        values.put(COLUMN_PHONE_NUMBER, contact.getPhoneNumber());
        values.put(COLUMN_CONTACT_GROUP, contact.getGroup());
        values.put(COLUMN_PHOTO_URI, contact.getPhotoUri());
        long newRowId = db.insert(TABLE_NAME, null, values);
        db.close();
        return newRowId;
    }

    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String nickname = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NICKNAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE_NUMBER));
                String group = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_GROUP));
                String photoUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_URI));
                contacts.add(new Contact(id, name, nickname, phoneNumber, group, photoUri));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return contacts;
    }

    public int updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, contact.getName());
        values.put(COLUMN_NICKNAME, contact.getNickname());
        values.put(COLUMN_PHONE_NUMBER, contact.getPhoneNumber());
        values.put(COLUMN_CONTACT_GROUP, contact.getGroup());
        values.put(COLUMN_PHOTO_URI, contact.getPhotoUri());
        int rowsUpdated = db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(contact.getId())});
        db.close();
        return rowsUpdated;
    }

    public int deleteContact(long contactId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(contactId)});
        db.close();
        return rowsDeleted;
    }

    public List<String> getAllGroups() {
        List<String> groups = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(true, TABLE_NAME, new String[]{COLUMN_CONTACT_GROUP}, null,
                null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String group = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_GROUP));
                groups.add(group);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return groups;
    }
}
