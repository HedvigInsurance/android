package com.hedvig.android.apollo

import arrow.core.Either

fun <T> OperationResult<T>.toEither(): Either<OperationResult.Error, T> = when (this) {
  is OperationResult.Error -> Either.Left(this)
  is OperationResult.Success -> Either.Right(this.data)
}

fun <ErrorType, T> OperationResult<T>.toEither(ifEmpty: ErrorType): Either<ErrorType, T> = when (this) {
  is OperationResult.Error -> Either.Left(ifEmpty)
  is OperationResult.Success -> Either.Right(this.data)
}

inline fun <ErrorType, T> OperationResult<T>.toEither(
  ifEmpty: (message: String?, throwable: Throwable?) -> ErrorType,
): Either<ErrorType, T> = when (this) {
  is OperationResult.Error -> Either.Left(ifEmpty(message, throwable))
  is OperationResult.Success -> Either.Right(this.data)
}
