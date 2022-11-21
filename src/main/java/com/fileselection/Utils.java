package com.fileselection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Utils {

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        drawable.setBounds(0,0, w, h);
        drawable.draw(canvas);

        return result;
    }


    public static String getFileType(String fileName){
        if(fileName == null) return null;
        if(TextUtils.isEmpty(fileName))return null;
        return fileName.substring(fileName.lastIndexOf(".") + 1);//获取文件的后缀名;
    }

    //将文件按名称排序
    public static File[] FileStoring(File[] files) {
        List fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && !o2.isDirectory())
                    return -1;
                if (!o1.isDirectory() && o2.isDirectory())
                    return 1;
                return o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
            }
        });
        return files;
    }

}
