package com.example.livestate.customView

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withTranslation

class HorizontalPanImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var bitmap: Bitmap? = null
    private var offsetX = 0f
    private var lastTouchX = 0f
    private var maxOffsetX = 0f

    fun setImageBitmap(bmp: Bitmap) {
        bitmap = bmp
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val bmp = bitmap
        if (bmp != null) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = bmp.height * width / bmp.width
            setMeasuredDimension(width, height)
            maxOffsetX = (bmp.width - width).toFloat()
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = bitmap ?: return

        val left = offsetX.coerceIn(0f, maxOffsetX)
        canvas.withTranslation(-left, 0f) {
            canvas.drawBitmap(bmp, 0f, 0f, null)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                offsetX = (offsetX - dx).coerceIn(0f, maxOffsetX)
                lastTouchX = event.x
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
