package com.example.contactapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.Random;

public class Contact implements Parcelable {
    private long id;
    private String name;
    private String nickname;
    private String phoneNumber;
    private String group;
    private String photoUri;

    // 构造函数
    public Contact(long id, String name, String nickname, String phoneNumber, String group, String photoUri) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.group = group;
        this.photoUri = photoUri;
    }

    // 从Parcel中读取数据的构造函数
    protected Contact(Parcel in) {
        id = in.readLong();
        name = in.readString();
        nickname = in.readString();
        phoneNumber = in.readString();
        group = in.readString();
        photoUri = in.readString();
    }

    // Parcelable接口的实现
    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeString(nickname);
        parcel.writeString(phoneNumber);
        parcel.writeString(group);
        parcel.writeString(photoUri);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // 创建包含字符的位图
    public Bitmap createBitmapFromCharacter(char character, int viewSize, int color, int padding) {
        Paint paint = new Paint();
        paint.setTextSize((float) (viewSize - padding * 2)); // 调整文本大小以适应视图
        paint.setColor(color);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);

        // 计算文本绘制的基线位置
        Rect textBounds = new Rect();
        paint.getTextBounds(String.valueOf(character), 0, 1, textBounds);
        int width = viewSize;
        int height = viewSize;
        float baseline = (height / 2 - (paint.descent() + paint.ascent()) / 2);

        // 创建位图并在其上绘制文本
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int backgroundColor = Color.rgb(new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256));
        canvas.drawColor(backgroundColor);
        canvas.drawText(String.valueOf(character), (float) (width / 2), baseline, paint);

        return bitmap;
    }

    // 获取和设置字段的方法
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }
}
