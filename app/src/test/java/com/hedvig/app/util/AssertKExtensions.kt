package com.hedvig.app.util

import assertk.Assert
import assertk.assertions.support.expected
import assertk.assertions.support.show

inline fun <reified T> Assert<Iterable<*>>.containsNoneOfType() = given { actual ->
    val notExpected = actual.filterIsInstance<T>()

    if (notExpected.isEmpty()) {
        return
    }

    expected(
        "to contain none of type:${show(T::class)} but was:${show(actual)}" +
            "\nelements not expected:${show(notExpected)}"
    )
}

inline fun <reified T> Assert<Iterable<*>>.containsOfType(amount: Int? = null) = given { actual ->
    val expected = actual.filterIsInstance<T>()

    if (expected.isNotEmpty()) {
        if (amount != null) {
            if (expected.size != amount) {
                expected(
                    "to contain $amount of type:${show(T::class)}" +
                        " but was ${expected.size} of type:${show(actual)}"
                )
            }
        }
        return
    }

    expected("to contain of type:${show(T::class)} but was:${show(actual)}")
}
