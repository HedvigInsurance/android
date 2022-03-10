package com.hedvig.app.util

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.Option

fun <T> List<T>.toNel(): Option<NonEmptyList<T>> {
    return NonEmptyList.fromList(this)
}

fun <E, T> List<T>.toNel(ifEmpty: () -> E): Either<E, NonEmptyList<T>> {
    return NonEmptyList.fromList(this).toEither { ifEmpty() }
}
