package com.hedvig.app.util

fun boundedLerp(start: Float, stop: Float, amount: Float): Float {
    val boundedAmount = when {
        amount > 1f -> 1f
        amount < 0f -> 0f
        else -> amount
    }
    return (1 - boundedAmount) * start + stop * boundedAmount
}
