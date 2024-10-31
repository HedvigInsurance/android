package com.hedvig.android.core.common.test

import arrow.core.Either
import assertk.Assert
import assertk.assertions.support.expected
import assertk.assertions.support.show

fun <T : Either<Left, *>, Left> Assert<T>.isLeft(): Assert<Left> = transform { actual ->
  when (actual) {
    is Either.Left<*> -> actual.value as Left
    is Either.Right<*> -> expected(
      "to be instance of:${show(Either.Left::class)} but was instance of:${show(Either.Right::class)}",
    )

    else -> error("Impossible")
  }
}

fun <T : Either<*, Right>, Right> Assert<T>.isRight(): Assert<Right> = transform { actual ->
  when (actual) {
    is Either.Left<*> -> expected(
      "to be instance of:${show(Either.Right::class)} but was instance of:${show(Either.Left::class)}",
    )

    is Either.Right<*> -> actual.value as Right
    else -> error("Impossible")
  }
}
