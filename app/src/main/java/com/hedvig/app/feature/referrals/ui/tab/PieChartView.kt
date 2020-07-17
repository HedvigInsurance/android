package com.hedvig.app.feature.referrals.ui.tab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

class PieChartView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attributeSet, defStyle) {

    private val circle = RectF()
    private val paintCache = HashMap<Int, Paint>()

    var segments: List<PieChartSegment> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    fun reveal(finalSegments: List<PieChartSegment>, onEnd: (() -> Unit)? = null) {
        val animation = SpringAnimation(FloatValueHolder())
            .apply {
                spring = SpringForce().apply {
                    dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                    stiffness = SpringForce.STIFFNESS_VERY_LOW
                }
            }
            .addUpdateListener { _, value, _ ->
                segments = finalSegments
                    .map { it.copy(percentage = it.percentage * value / 100) }
            }

        onEnd?.let {
            animation.addEndListener { _, canceled, _, _ ->
                if (!canceled) {
                    onEnd()
                }
            }
        }
        animation.animateToFinalPosition(ONE_HUNDRED_PERCENT)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val desiredHeight = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val size = when {
            widthMode == MeasureSpec.EXACTLY && desiredWidth > 0 -> desiredWidth
            heightMode == MeasureSpec.EXACTLY && desiredHeight > 0 -> desiredHeight
            else -> if (desiredWidth < desiredHeight) {
                desiredWidth
            } else {
                desiredHeight
            }
        }

        val resolvedWidth = resolveSize(size, widthMeasureSpec)
        val resolvedHeight = resolveSize(size, heightMeasureSpec)

        circle.set(
            0f,
            0f,
            resolvedWidth.toFloat(),
            resolvedHeight.toFloat()
        )

        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        var startPosition =
            ANGLE_UP

        segments.forEach { segment ->
            val sweep = segment.percentage * DEGREES_PER_PERCENT_RATIO
            val paint = paintCache[segment.color] ?: createPaint(segment.color)
            canvas?.drawArc(circle, startPosition, sweep, true, paint)
            startPosition += sweep
        }
    }

    private fun createPaint(@ColorInt color: Int): Paint {
        val paint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
            flags = Paint.ANTI_ALIAS_FLAG
        }
        paintCache[color] = paint
        return paint
    }

    companion object {
        private const val ONE_HUNDRED_PERCENT = 100f
        private const val DEGREES_PER_PERCENT_RATIO = 3.6f
        private const val ANGLE_UP = 270f
    }
}

data class PieChartSegment(
    val id: Int,
    val percentage: Float,
    @ColorInt val color: Int
)
