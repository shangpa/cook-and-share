package com.example.test.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CropGuideView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // 0~1 정규화 좌표
    private var leftN = 0f
    private var topN = 0f
    private var rightN = 1f
    private var bottomN = 1f

    private val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
        color = Color.WHITE
    }
    private val corner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(3f)
        color = Color.WHITE
    }
    private val mask = Paint().apply {
        color = Color.BLACK
        alpha = 90
    }

    fun setCropNormalized(l: Float, t: Float, r: Float, b: Float) {
        leftN = l.coerceIn(0f, 1f)
        topN = t.coerceIn(0f, 1f)
        rightN = r.coerceIn(0f, 1f)
        bottomN = b.coerceIn(0f, 1f)
        if (rightN < leftN) rightN = leftN
        if (bottomN < topN) bottomN = topN
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val left = leftN * w
        val top = topN * h
        val right = rightN * w
        val bottom = bottomN * h
        val rect = RectF(left, top, right, bottom)

        // 바깥 마스킹
        canvas.drawRect(0f, 0f, w, top, mask)
        canvas.drawRect(0f, bottom, w, h, mask)
        canvas.drawRect(0f, top, left, bottom, mask)
        canvas.drawRect(right, top, w, bottom, mask)

        // 테두리
        canvas.drawRect(rect, border)

        // 모서리
        val L = dp(16f)
        // 좌상
        canvas.drawLine(left, top, left + L, top, corner)
        canvas.drawLine(left, top, left, top + L, corner)
        // 우상
        canvas.drawLine(right, top, right - L, top, corner)
        canvas.drawLine(right, top, right, top + L, corner)
        // 좌하
        canvas.drawLine(left, bottom, left + L, bottom, corner)
        canvas.drawLine(left, bottom, left, bottom - L, corner)
        // 우하
        canvas.drawLine(right, bottom, right - L, bottom, corner)
        canvas.drawLine(right, bottom, right, bottom - L, corner)
    }

    private fun dp(v: Float) = v * resources.displayMetrics.density
}
