package com.hedvig.app.feature.referrals.ui

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

    private val circle = RectF(0f, 0f, 1000f, 1000f)
    private val colorStash = HashMap<Int, Paint>()

    var segments: List<PieChartSegment> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    fun reveal(finalSegments: List<PieChartSegment>) {
        SpringAnimation(FloatValueHolder())
            .apply {
                spring = SpringForce().apply {
                    dampingRatio = 0.65f
                    stiffness = SpringForce.STIFFNESS_VERY_LOW
                }
            }
            .addUpdateListener { _, value, _ ->
                segments = finalSegments
                    .map { it.copy(percentage = it.percentage * value / 100) }
            }
            .animateToFinalPosition(ONE_HUNDRED_PERCENT)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        var startPosition = 0f

        segments.forEach { segment ->
            val sweep = -(segment.percentage * DEGREES_PER_PERCENT_RATIO)
            val paint = colorStash[segment.color] ?: createColor(segment.color)
            canvas?.drawArc(circle, startPosition, sweep, true, paint)
            startPosition += sweep
        }
    }

    private fun createColor(@ColorInt color: Int): Paint {
        val paint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
            flags = Paint.ANTI_ALIAS_FLAG
        }
        colorStash[color] = paint
        return paint
    }

    companion object {
        private const val ONE_HUNDRED_PERCENT = 100f
        private const val DEGREES_PER_PERCENT_RATIO = 3.6f
    }
}

data class PieChartSegment(
    val percentage: Float,
    @ColorInt val color: Int
)
