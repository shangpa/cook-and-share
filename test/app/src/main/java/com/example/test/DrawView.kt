package com.example.test.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val path = Path()
    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 8f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val textList = mutableListOf<Pair<String, PointF>>()
    private var mode: Mode = Mode.DRAW

    private var cropRect: RectF? = null
    private var startX = 0f
    private var startY = 0f

    fun setMode(newMode: Mode) {
        mode = newMode
    }

    fun addText(text: String, x: Float, y: Float) {
        textList.add(Pair(text, PointF(x, y)))
        invalidate()
    }

    fun setPaintColor(color: Int) {
        paint.color = color
        paint.xfermode = null // 지우개 모드 해제
    }

    fun enableEraser() {
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }


    fun getCropRect(): RectF? = cropRect

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (mode) {
            Mode.DRAW -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> path.moveTo(event.x, event.y)
                    MotionEvent.ACTION_MOVE -> path.lineTo(event.x, event.y)
                }
            }
            Mode.CROP -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.x
                        startY = event.y
                        cropRect = RectF(startX, startY, startX, startY)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        cropRect?.set(
                            minOf(startX, event.x),
                            minOf(startY, event.y),
                            maxOf(startX, event.x),
                            maxOf(startY, event.y)
                        )
                    }
                }
            }
            Mode.TEXT -> {
                // 현재는 터치에 반응 안 해도 되니까 아무것도 안 해도 돼냥
            }
        }
        invalidate()
        return true
    }


    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
        textList.forEach { (text, point) ->
            canvas.drawText(text, point.x, point.y, paint.apply {
                color = Color.BLUE
                textSize = 50f
                style = Paint.Style.FILL
            })
        }
        cropRect?.let {
            canvas.drawRect(it, paint.apply {
                color = Color.GREEN
                style = Paint.Style.STROKE
                strokeWidth = 5f
            })
        }
    }

    enum class Mode { DRAW, TEXT, CROP }
}
