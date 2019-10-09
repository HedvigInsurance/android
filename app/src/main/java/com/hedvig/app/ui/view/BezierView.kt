package com.hedvig.app.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.hedvig.app.R
import kotlin.math.min

class BezierView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        initialize(attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        initialize(attributeSet)
    }

    private val topPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val bottomPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    @ColorInt
    var topColor: Int = 0
        set(value) {
            field = value
            topPaint.color = value
        }

    @ColorInt
    var bottomColor: Int = 0
        set(value) {
            field = value
            bottomPaint.color = value
        }

    private val path = Path()

    private fun initialize(attributeSet: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.BezierView,
            0,
            0
        )

        topColor = attributes.getColor(R.styleable.BezierView_topColor, 0)
        bottomColor = attributes.getColor(R.styleable.BezierView_bottomColor, 0)

        attributes.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val fWidth = width.toFloat()
        val fDesiredHeight = DESIRED_HEIGHT.toFloat()

        path.reset()
        path.moveTo(0f, fDesiredHeight)
        path.cubicTo(
            fWidth * 0.5f,
            fDesiredHeight,
            fWidth * 0.6f,
            0f,
            fWidth,
            fDesiredHeight
        )
        path.lineTo(fWidth, 0f)
        path.lineTo(0f, 0f)
        path.lineTo(0f, fDesiredHeight)
        canvas?.drawPath(path, topPaint)

        path.reset()
        path.moveTo(0f, fDesiredHeight)
        path.cubicTo(fWidth * 0.5f, fDesiredHeight, fWidth * 0.6f, 0f, fWidth, fDesiredHeight)
        path.lineTo(0f, fDesiredHeight)
        canvas?.drawPath(path, bottomPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val resolvedHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(heightSize, DESIRED_HEIGHT)
            else -> DESIRED_HEIGHT
        }

        setMeasuredDimension(widthSize, resolvedHeight)
    }

    companion object {
        const val DESIRED_HEIGHT = 200
    }
}
