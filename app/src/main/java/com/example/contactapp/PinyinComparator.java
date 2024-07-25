package com.example.contactapp;

import com.example.contactapp.Contact;
import java.util.Comparator;
import net.sourceforge.pinyin4j.PinyinHelper;
public class PinyinComparator implements Comparator<Contact> {
    @Override
    public int compare(Contact c1, Contact c2) {
        return toPinyin(c1.getName()).compareTo(toPinyin(c2.getName()));
    }

    private String toPinyin(String chinese) {
        StringBuilder pinyin = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            String[] p = PinyinHelper.toHanyuPinyinStringArray(c);
            if (p != null) {
                pinyin.append(p[0]);
            } else {
                pinyin.append(c);
            }
        }
        return pinyin.toString();
    }
}
