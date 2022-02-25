package com.hedvig.app.util

import arrow.core.Either

fun <T> Either<T, T>.getLeftAndRight(): T = when (this) {
    is Either.Left -> this.value
    is Either.Right -> this.value
}
