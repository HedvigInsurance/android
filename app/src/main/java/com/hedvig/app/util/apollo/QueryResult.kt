package com.hedvig.app.util.apollo

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some

sealed interface QueryResult<out T> {
  data class Success<T>(val data: T) : QueryResult<T>
  sealed class Error : QueryResult<Nothing> {

    abstract val message: String?

    data class NoDataError(override val message: String?) : Error()
    data class GeneralError(override val message: String?) : Error()
    data class QueryError(override val message: String?) : Error()
    data class NetworkError(override val message: String?) : Error()
  }
}

fun <T> QueryResult<T>.toOption(): Option<T> = when (this) {
  is QueryResult.Error -> None
  is QueryResult.Success -> Some(this.data)
}

fun <T> QueryResult<T>.toEither(): Either<QueryResult.Error, T> = when (this) {
  is QueryResult.Error -> Either.Left(this)
  is QueryResult.Success -> Either.Right(this.data)
}

inline fun <ErrorType, T> QueryResult<T>.toEither(
  ifEmpty: (message: String?) -> ErrorType,
): Either<ErrorType, T> = when (this) {
  is QueryResult.Error -> Either.Left(ifEmpty(message))
  is QueryResult.Success -> Either.Right(this.data)
}
