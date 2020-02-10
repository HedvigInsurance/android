package com.hedvig.app.util

import android.view.View
import androidx.core.util.Pair
import androidx.core.view.ViewCompat

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

fun transitionPair(view: View) = Pair(view, ViewCompat.getTransitionName(view)!!)
