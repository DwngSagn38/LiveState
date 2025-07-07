package com.example.livestate.ui.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class SpeedometerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var speed = 0f
    private val maxSpeed = 120f

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        style = Paint.Style.FILL
    }

    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 10f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 48f
        textAlign = Paint.Align.CENTER
    }

    fun setSpeed(kmh: Float) {
        speed = kmh.coerceIn(0f, maxSpeed)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) * 0.9f

        canvas.drawCircle(cx, cy, radius, backgroundPaint)

        val angle = Math.toRadians(135.0 - (speed / maxSpeed * 270))
        val length = radius * 0.75f
        val endX = cx + length * cos(angle).toFloat()
        val endY = cy - length * sin(angle).toFloat()
        canvas.drawLine(cx, cy, endX, endY, needlePaint)

        canvas.drawText("${speed.toInt()} km/h", cx, cy + radius * 0.6f, textPaint)
    }
}
