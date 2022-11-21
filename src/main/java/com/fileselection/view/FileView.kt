package com.fileselection.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.io.File
import java.text.DecimalFormat

/**
 * 文件可视化View
 */
class FileView : FView {

    private var paint = Paint()
    private var size = 0L

    constructor(context: Context, icon: Bitmap, file: File): super(context, icon, file.name) {
        paint.color = Color.parseColor("#615F5F")
        size = file.length()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        paint.textSize = measuredHeight/6f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText("文件大小："+sizeTo(size), height+30f, height-paint.textSize, paint)
    }

    private fun sizeTo(size: Long): String {
        var result = ""
        if (size < 1024)
            result = ""+size+"B"
        else if (size < (1024*1024))
            result = ""+DecimalFormat("#.00").format(size/1024.0)+"KB"
        else if (size < (1024*1024*1024))
            result = ""+DecimalFormat("#.00").format(size/1024/1024.0)+"MB"
        else if (size < (1024L*1024*1024*1024))
            result = ""+DecimalFormat("#.00").format(size/1024/1024/1024.0)+"GB"
        else
            result = ""+DecimalFormat("#.00").format(size/1024/1024/1024/1024.0)+"TB"

        return result
    }

}