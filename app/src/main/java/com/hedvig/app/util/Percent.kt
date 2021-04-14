package com.hedvig.app.util

import java.lang.IllegalArgumentException

data class Percent(val value: Int) {
    init {
        if (value !in 0..100) {
            throw IllegalArgumentException("Percentage must be within 0..100, value: $value")
        }
    }

    fun toFraction() = value / 100f
}
