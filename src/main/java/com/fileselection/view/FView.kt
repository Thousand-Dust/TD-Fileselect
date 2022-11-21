package com.fileselection.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import java.io.File
import kotlin.concurrent.thread

/**
 * 可视化文件View
 */
open class FView : View {

    private var icon: Bitmap
    var filename: String
        private set
    private var paint: Paint
    /**
     * 按下反馈效果属性
     */
    private var downPaint: Paint
    private var downRect: RectF

    var isShow = false
        private set

    constructor(context: Context, icon: Bitmap, filename: String): super(context) {
        /**
         * 对图片缩放至边长和view高度一样
         */
        val matrix = Matrix()
        matrix.postScale(120f/icon.width, 120f/icon.height)
        this.icon = Bitmap.createBitmap(icon, 0,0, icon.width, icon.height, matrix, true)

        this.filename = filename

        paint = Paint()
        paint.strokeWidth = 0.5f
        //设置画出的文字大小是view高度的一半
        paint.isAntiAlias = true

        downPaint = Paint()
        downPaint.setColor(Color.parseColor("#3275BFCC"))
        downPaint.style = Paint.Style.FILL

        downRect = RectF()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var measureWidth = measuredWidth
        var measureHeight = measuredHeight

        val dm = context.resources.displayMetrics
        measureWidth = resolveSize(dm.widthPixels, measureWidth) //使用最大的宽值
        measureHeight = resolveSize(150, measureHeight)
        paint.textSize = measureHeight * 0.3f

        setMeasuredDimension(measureWidth, measureHeight)
    }

    override fun onDraw(canvas: Canvas) {
        //画背景
        canvas.drawColor(Color.parseColor("#FFFFFF"))
        //画图标
        canvas.drawBitmap(icon, 0f, 0f, paint)
        //画文件名
        canvas.drawText(filename, height+30f, (height-paint.textSize)/1.5f, paint)
        //画分界线
        canvas.drawLine(height.toFloat(), height-1f, width.toFloat(), height-1f, paint)
        //是否选中
        if (isShow) {
            canvas.drawRect(downRect, downPaint)
        }
    }

    private var downAnimator: ValueAnimator? = null
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var result = super.onTouchEvent(event)
        if (event != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //手指按下
                    isShow = true
                    var end = 0f
                    val finalx = event.x
                    if (finalx <= width/2) end = (width-finalx).toFloat()
                    else end = finalx
                    downAnimator?.let {
                        it.cancel()
                    }
                    downAnimator = ValueAnimator.ofFloat(0f, end)
                    downAnimator?.let {
                        it.setDuration(200)
                        it.addUpdateListener {
                            val value = it.animatedValue as Float
                            downRect.set(finalx - value, 0f, finalx + value, height.toFloat())
                            invalidate()
                        }
                        it.start()
                    }
                    result = true
                }
                MotionEvent.ACTION_MOVE -> {
                    //手指滑动
                    result = true
                }

                else -> {
                    downAnimator?.let {
                        it.end()
                    }
                    invalidate()
                    thread {
                        Thread.sleep(100)
                        isShow = false
                        postInvalidate()
                    }
                    result = false
                }
            }
        }

        return result
    }

}