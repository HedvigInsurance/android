package com.hedvig.app.util.apollo

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some

sealed interface QueryResult<out T> {

  data class Success<T>(val data: T) : QueryResult<T>

  sealed interface Error : QueryResult<Nothing> {

    val throwable: Throwable?
    val message: String?

    data class NoDataError(
      override val throwable: Throwable?,
      override val message: String?,
    ) : Error {
      companion object {
        operator fun invoke(message: String?): NoDataError {
          return NoDataError(null, message)
        }

        operator fun invoke(throwable: Throwable?): NoDataError {
          return NoDataError(throwable, throwable?.localizedMessage)
        }
      }
    }

    data class GeneralError(
      override val throwable: Throwable?,
      override val message: String?,
    ) : Error {
      companion object {
        operator fun invoke(message: String?): GeneralError {
          return GeneralError(null, message)
        }

        operator fun invoke(throwable: Throwable?): GeneralError {
          return GeneralError(throwable, throwable?.localizedMessage)
        }
      }
    }

    data class QueryError(
      override val throwable: Throwable?,
      override val message: String?,
    ) : Error {
      companion object {
        operator fun invoke(message: String?): QueryError {
          return QueryError(null, message)
        }

        operator fun invoke(throwable: Throwable?): QueryError {
          return QueryError(throwable, throwable?.localizedMessage)
        }
      }
    }

    data class NetworkError(
      override val throwable: Throwable?,
      override val message: String?,
    ) : Error {
      companion object {
        operator fun invoke(message: String?): NetworkError {
          return NetworkError(null, message)
        }

        operator fun invoke(throwable: Throwable?): NetworkError {
          return NetworkError(throwable, throwable?.localizedMessage)
        }
      }
    }
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
