package com.hedvig.app.util

@JvmInline
value class Percent(val value: Int) {
    init {
        require(value in 0..100)
    }

    fun toFraction() = value / 100f
}
