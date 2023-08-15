package com.hedvig.android.core.common.test

import arrow.core.Either
import assertk.Assert
import assertk.assertions.support.expected
import assertk.assertions.support.show

fun <T : Either<Left, *>, Left> Assert<T>.isLeft(): Assert<Left> = transform { actual ->
  actual.leftOrNull()
    ?: expected("to be instance of:${show(Either.Left::class)} but was instance of:${show(Either.Right::class)}")
}

fun <T : Either<*, Right>, Right> Assert<T>.isRight(): Assert<Right> = transform { actual ->
  actual.getOrNull()
    ?: expected("to be instance of:${show(Either.Right::class)} but was instance of:${show(Either.Left::class)}")
}
