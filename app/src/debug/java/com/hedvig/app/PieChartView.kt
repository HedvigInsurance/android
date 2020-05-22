package com.hedvig.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class PieChartView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attributeSet, defStyle) {

    private val circle = RectF(0f, 0f, 200f, 200f)
    private val colors = listOf(
        Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        },
        Paint().apply {
            color = Color.BLUE
            style = Paint.Style.FILL
        }
    )

    var segments: List<PieChartSegment> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        var startPosition = 0f

        segments.forEachIndexed { index, segment ->
            val sweep = segment.percentage * 3.6f
            canvas?.drawArc(circle, startPosition, sweep, true, colors[index])
            startPosition += sweep
        }
    }
}
