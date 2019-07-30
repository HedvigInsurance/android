package com.hedvig.app.util.extensions.view

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.hedvig.app.R

fun View.animateExpand(
    duration: Long = 200,
    interpolator: TimeInterpolator = DecelerateInterpolator(),
    updateCallback: (() -> Unit)? = null,
    withOpacity: Boolean = false
) {
    show()
    val parentWidth = (parent as View).measuredWidth
    measure(
        View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY),
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    val targetHeight = measuredHeight + if (this is TextView) paint.fontSpacing.toInt() else 0

    val currentHeight = height
    ValueAnimator.ofInt(currentHeight, targetHeight).apply {
        addUpdateListener { animation ->
            layoutParams.height = animation.animatedValue as Int
            if (withOpacity) {
                alpha = animation.animatedFraction
            }
            requestLayout()
            updateCallback?.invoke()
        }
        this.interpolator = interpolator
        this.duration = duration
        start()
    }
}

fun View.animateCollapse(
    targetHeight: Int = 0,
    duration: Long = 200,
    interpolator: TimeInterpolator = DecelerateInterpolator(),
    withOpacity: Boolean = false
) {
    measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    val currentHeight = height
    show()
    ValueAnimator.ofInt(currentHeight, targetHeight).apply {
        addUpdateListener { animation ->
            layoutParams.height = animation.animatedValue as Int
            if (withOpacity) {
                alpha = 1.0f - animation.animatedFraction
            }
            requestLayout()
        }

        this.interpolator = interpolator
        this.duration = duration
        start()
    }
}

fun View.spring(
    property: FloatPropertyCompat<View>,
    damping: Float = SpringForce.DAMPING_RATIO_NO_BOUNCY,
    stiffness: Float = 500f
): SpringAnimation {
    val key = getKey(property)
    return getTag(key) as? SpringAnimation ?: {
        val anim = SpringAnimation(this, property)
        setTag(key, anim)
        anim.spring = SpringForce().apply {
            dampingRatio = damping
            this.stiffness = stiffness
        }
        anim
    }()
}

private fun getKey(property: FloatPropertyCompat<View>) = when (property) {
    SpringAnimation.SCALE_X -> R.id.spring_scale_x
    SpringAnimation.SCALE_Y -> R.id.spring_scale_y
    SpringAnimation.TRANSLATION_Y -> R.id.spring_translation_y
    else -> {
        throw RuntimeException("No key for ViewProperty $property")
    }
}
