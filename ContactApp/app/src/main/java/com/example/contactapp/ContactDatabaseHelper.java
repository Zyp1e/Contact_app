package com.example.contactapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于管理联系人数据库的帮助类，继承自 SQLiteOpenHelper
 */
public class ContactDatabaseHelper extends SQLiteOpenHelper {

    // 数据库版本
    private static final int DATABASE_VERSION = 1;
    // 数据库名称
    private static final String DATABASE_NAME = "contacts.db";
    // 表名称
    public static final String TABLE_NAME = "contacts";
    // 列名常量
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_NICKNAME = "nickname";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_CONTACT_GROUP = "contact_group";
    public static final String COLUMN_PHOTO_URI = "photo_uri";

    // 构造函数
    public ContactDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 创建数据库表的方法
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建联系人表的 SQL 语句
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME + " TEXT," +
                COLUMN_NICKNAME + " TEXT," +
                COLUMN_PHONE_NUMBER + " TEXT," +
                COLUMN_CONTACT_GROUP + " TEXT," +
                COLUMN_PHOTO_URI + " TEXT)";
        db.execSQL(createTable);
    }

    /**
     * 更新数据库表的方法
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 删除旧表，如果存在的话
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // 重新创建表
        onCreate(db);
    }

    /**
     * 插入联系人到数据库的方法
     *
     * @param contact 要插入的联系人对象
     * @return 插入的行ID
     */
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

    /**
     * 获取所有联系人的方法
     *
     * @return 联系人列表
     */
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

    /**
     * 更新联系人的方法
     *
     * @param contact 要更新的联系人对象
     * @return 更新的行数
     */
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

    /**
     * 删除联系人的方法
     *
     * @param contactId 要删除的联系人ID
     * @return 删除的行数
     */
    public int deleteContact(long contactId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(contactId)});
        db.close();
        return rowsDeleted;
    }

    /**
     * 获取所有分组的方法
     *
     * @return 分组列表
     */
//对应loadSettings
    public List<String> getAllGroups() {
        List<String> groups = new ArrayList<>(); // 创建一个空的字符串列表，用于存储分组名称
        SQLiteDatabase db = this.getReadableDatabase(); // 获取可读数据库实例
        Cursor cursor = db.query(true, TABLE_NAME, new String[]{COLUMN_CONTACT_GROUP}, null,
                null, null, null, null, null);
        // 执行查询操作，查询所有不重复的联系人分组名

        if (cursor != null && cursor.moveToFirst()) {
            // 如果查询结果非空且移动到第一行成功，则进入循环
            do {
                String group = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_GROUP));
                // 从游标中获取分组名数据
                groups.add(group); // 将分组名添加到列表中
            } while (cursor.moveToNext()); // 移动游标到下一行，继续循环

            cursor.close(); // 关闭游标，释放资源
        }

        db.close(); // 关闭数据库连接，释放资源
        return groups; // 返回所有查询到的不重复分组名列表
    }
}

