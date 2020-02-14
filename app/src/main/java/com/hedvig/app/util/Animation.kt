package com.hedvig.app.util

import android.view.View
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import kotlin.math.abs

fun boundedLerp(start: Float, stop: Float, amount: Float): Float {
    val boundedAmount = when {
        amount > 1f -> 1f
        amount < 0f -> 0f
        else -> amount
    }
    return (1 - boundedAmount) * start + stop * boundedAmount
}

fun boundedLerp(start: Int, stop: Int, amount: Float) =
    boundedLerp(start.toFloat(), stop.toFloat(), amount).toInt()

fun boundedProgress(start: Float, end: Float, current: Float): Float {
    val boundedCurrent = when {
        start > end && current > start -> start
        start > end && current < end -> end
        end > start && current < start -> start
        end > start && current > end -> end
        else -> current
    }

    return if (start > end) {
        1 - boundedCurrent / abs(end - start)
    } else {
        boundedCurrent / abs(end - start)
    }
}

// 175.0, 0.0, 180 => 175.0
// 175.0, 0.0, 168 => 168
// 175.0, 0.0, -5 => 0
// 0.0, 175.0, 168 => 168
// 0.0, 175.0, 180 => 175.0

fun transitionPair(view: View) = Pair(view, ViewCompat.getTransitionName(view)!!)
