package com.example.contactapp;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class ContactExporter {
    public static void exportContactsToUri(Context context, Uri uri) {
        ContactDatabaseHelper dbHelper = new ContactDatabaseHelper(context);
        List<Contact> contacts = dbHelper.getAllContacts();

        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);

            // 写入 CSV 文件的头部
            writer.append("ID,姓名,别名,电话,分组,图片\n");

            // 逐行写入联系人数据
            for (Contact contact : contacts) {
                writer.append(String.valueOf(contact.getId())).append(",");
                writer.append(contact.getName()).append(",");
                writer.append(contact.getNickname()).append(",");
                writer.append(contact.getPhoneNumber()).append(",");
                writer.append(contact.getGroup()).append(",");
                writer.append(contact.getPhotoUri()).append("\n");
            }

            writer.flush();
            writer.close();

            Toast.makeText(context, "联系人导出成功!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("ContactExporter", "导出联系人失败", e);
            Toast.makeText(context, "导出联系人失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

