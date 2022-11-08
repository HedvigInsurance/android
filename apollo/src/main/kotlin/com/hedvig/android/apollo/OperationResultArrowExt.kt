package com.hedvig.android.apollo

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some

fun <T> OperationResult<T>.toOption(): Option<T> = when (this) {
  is OperationResult.Error -> None
  is OperationResult.Success -> Some(this.data)
}

fun <T> OperationResult<T>.toEither(): Either<OperationResult.Error, T> = when (this) {
  is OperationResult.Error -> Either.Left(this)
  is OperationResult.Success -> Either.Right(this.data)
}

inline fun <ErrorType, T> OperationResult<T>.toEither(
  ifEmpty: (message: String?) -> ErrorType,
): Either<ErrorType, T> = when (this) {
  is OperationResult.Error -> Either.Left(ifEmpty(message))
  is OperationResult.Success -> Either.Right(this.data)
}
