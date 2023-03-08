package com.hedvig.android.apollo

import arrow.core.Either

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
