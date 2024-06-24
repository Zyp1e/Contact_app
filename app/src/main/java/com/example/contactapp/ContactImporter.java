package com.example.contactapp;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ContactImporter {
    public static void importContactsFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            List<Contact> contacts = new ArrayList<>();

            // 读取 CSV 文件的头部
            reader.readLine();

            // 逐行读取 CSV 数据
            while ((line = reader.readLine()) != null) {
                String[] rowData = line.split(",");
                if (rowData.length > 5) {
                    long id = Long.parseLong(rowData[0]);
                    String name = rowData[1];
                    String nickname = rowData[2];
                    String phoneNumber = rowData[3];
                    String group = rowData[4];
                    String photoUri = rowData[5];
                    contacts.add(new Contact(id, name, nickname, phoneNumber, group, photoUri));
                }
            }

            reader.close();

            // 将读取到的联系人存储到数据库或更新到界面上
            ContactDatabaseHelper dbHelper = new ContactDatabaseHelper(context);
            for (Contact contact : contacts) {
                dbHelper.insertContact(contact);
            }

            Toast.makeText(context, "联系人导入成功!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("ContactImporter", "导入联系人失败", e);
            Toast.makeText(context, "导入联系人失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
