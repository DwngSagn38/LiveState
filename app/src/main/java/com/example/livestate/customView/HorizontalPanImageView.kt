package com.example.livestate.customView

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.*
import kotlin.math.max
import kotlin.math.min

class HorizontalPanImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var bitmap: Bitmap? = null
    private var scaleFactor = 1f
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())

    // Offsets dùng trong hệ toạ độ gốc (ảnh gốc, chưa scale)
    private var offsetX = 0f
    private var offsetY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private var maxOffsetX = 0f
    private var maxOffsetY = 0f

    fun setImageBitmap(bmp: Bitmap) {
        bitmap = bmp
        scaleFactor = 1f
        offsetX = 0f
        offsetY = 0f
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val bmp = bitmap
        if (bmp != null) {
            val viewWidth = MeasureSpec.getSize(widthMeasureSpec)
            val viewHeight = bmp.height * viewWidth / bmp.width
            setMeasuredDimension(viewWidth, viewHeight)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = bitmap ?: return

        canvas.save()
        // Zoom around top-left origin, but we apply pan in scaled space
        canvas.translate(-offsetX * scaleFactor, -offsetY * scaleFactor)
        canvas.scale(scaleFactor, scaleFactor)
        canvas.drawBitmap(bmp, 0f, 0f, null)
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        if (!scaleGestureDetector.isInProgress && event.pointerCount == 1) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.x - lastTouchX) / scaleFactor
                    val dy = (event.y - lastTouchY) / scaleFactor
                    offsetX = (offsetX - dx).coerceIn(0f, maxOffsetX)
                    offsetY = (offsetY - dy).coerceIn(0f, maxOffsetY)
                    lastTouchX = event.x
                    lastTouchY = event.y
                    invalidate()
                }
            }
        }

        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val bmp = bitmap ?: return false
            val prevScale = scaleFactor
            val scaleChange = detector.scaleFactor
            val newScale = (scaleFactor * scaleChange).coerceIn(1f, 4f)

            // focus point trong hệ toạ độ gốc
            val focusX = detector.focusX
            val focusY = detector.focusY

            // Tính toạ độ focus trong bitmap
            val focusXInBitmap = (offsetX + focusX / prevScale)
            val focusYInBitmap = (offsetY + focusY / prevScale)

            // Cập nhật scale
            scaleFactor = newScale

            // Tính lại offset sao cho giữ focus tại chỗ cũ
            offsetX = focusXInBitmap - focusX / scaleFactor
            offsetY = focusYInBitmap - focusY / scaleFactor

            // Cập nhật giới hạn
            updateMaxOffsets(bmp)
            offsetX = offsetX.coerceIn(0f, maxOffsetX)
            offsetY = offsetY.coerceIn(0f, maxOffsetY)

            invalidate()
            return true
        }
    }

    private fun updateMaxOffsets(bmp: Bitmap) {
        val scaledWidth = bmp.width * scaleFactor
        val scaledHeight = bmp.height * scaleFactor

        maxOffsetX = max(0f, (scaledWidth - width) / scaleFactor)
        maxOffsetY = max(0f, (scaledHeight - height) / scaleFactor)
    }
}
