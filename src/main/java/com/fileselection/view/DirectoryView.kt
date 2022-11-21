package com.fileselection.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.io.File

/**
 * 文件夹可视化View
 */
class DirectoryView : FView {

    private var quantity = 0
    private var paint = Paint()

    constructor(context: Context, icon: Bitmap, file: File): super(context, icon, file.name) {
        paint.color = Color.parseColor("#615F5F")
        file.listFiles()?.let {
            quantity = it.size
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        paint.textSize = measuredHeight/6f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText("文件数量："+quantity, height+30f, height-paint.textSize, paint)
    }


}