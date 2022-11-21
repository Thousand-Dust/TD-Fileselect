package com.fileselection.data;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 自定义FileView属性
 */
public class FileViewAttributes {

    private int size;
    private String[][] suffixs;
    private Bitmap[] icon;

    /**
     * 传入自定义后缀名和对应图标
     * @param suffix 后缀名，使用数组中的第一个来作为图标id
     * @param icon 图标
     */
    public void put(String suffix[], Bitmap icon) {
        if (suffix == null) throw new NullPointerException("Suffix参数不能为空");
        if (suffix.length < 1) throw new NullPointerException("Suffix数组长度不能小于1");
        if (icon == null) throw new NullPointerException("Icon参数不能为空");
        for (int i = 0; i < suffix.length; i++) {
            if (suffix[i] == null) throw new NullPointerException("Suffix["+i+"]不能为空");
        }
        this.suffixs[size] = suffix;
        this.icon[size] = icon;
        size++;
    }

    public void put(String suffix[], Resources resources, int iconResId) {
        put(suffix, BitmapFactory.decodeResource(resources, iconResId));
    }

    public int length() {
        return size;
    }

    public String[] getSuffixs(int i) {
        return suffixs[i];
    }

    public Bitmap getIcon(int i) {
        return icon[i];
    }

}
