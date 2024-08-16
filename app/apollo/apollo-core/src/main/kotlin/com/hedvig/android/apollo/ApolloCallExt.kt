package com.hedvig.android.apollo

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.CacheMissException
import com.hedvig.android.apollo.ApolloOperationError.CacheMiss
import com.hedvig.android.apollo.ApolloOperationError.OperationError
import com.hedvig.android.apollo.ApolloOperationError.OperationException
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface ApolloOperationError {
  val throwable: Throwable?

  data class CacheMiss(override val throwable: CacheMissException) : ApolloOperationError {
    override fun toString(): String {
      return "CacheMiss(throwableMessage=${throwable.message}, throwable=$throwable)"
    }
  }

  data class OperationException(override val throwable: ApolloException) : ApolloOperationError {
    override fun toString(): String {
      return "OperationException(throwableMessage=${throwable.message}, throwable=$throwable)"
    }
  }

  data class OperationError(private val message: String) : ApolloOperationError {
    override val throwable: Throwable? = null
  }
}

suspend fun <D : Operation.Data> ApolloCall<D>.safeExecute(): Either<ApolloOperationError, D> {
  return either {
    parseResponse(execute())
  }
}

suspend fun <D : Operation.Data, ErrorType> ApolloCall<D>.safeExecute(
  mapError: (ApolloOperationError) -> ErrorType,
): Either<ErrorType, D> {
  return safeExecute().mapLeft(mapError)
}

fun <D : Operation.Data> ApolloCall<D>.safeFlow(): Flow<Either<ApolloOperationError, D>> {
  return toFlow().map {
    either {
      parseResponse(it)
    }
  }
}

fun <D : Operation.Data, ErrorType> ApolloCall<D>.safeFlow(
  mapError: (ApolloOperationError) -> ErrorType,
): Flow<Either<ErrorType, D>> {
  return safeFlow().map { it.mapLeft(mapError) }
}

fun ErrorMessage(apolloOperationError: ApolloOperationError): ErrorMessage = object : ErrorMessage {
  override val message = when (apolloOperationError) {
    is CacheMiss -> "Cache miss"
    is OperationError -> apolloOperationError.toString()
    is OperationException -> apolloOperationError.throwable.message
  }
  override val throwable = when (apolloOperationError) {
    is CacheMiss -> apolloOperationError.throwable
    is OperationError -> null
    is OperationException -> apolloOperationError.throwable
  }

  override fun toString(): String {
    return "ErrorMessage(message=$message, throwable=$throwable)"
  }
}

// https://www.apollographql.com/docs/kotlin/essentials/errors/#truth-table
private fun <D : Operation.Data> Raise<ApolloOperationError>.parseResponse(response: ApolloResponse<D>): D {
  val exception = response.exception
  val data = response.data
  val errors = response.errors
  if (exception == null && data == null && errors == null) {
    error("Non compliant server")
  }
  if (exception != null && errors != null) {
    error("Impossible. Exceptions and errors can't exist at the same time")
  }
  if (exception != null && data != null) {
    error("Impossible. Exceptions and data can't exist at the same time")
  }
  if (exception != null) {
    if (exception is CacheMissException) {
      raise(ApolloOperationError.CacheMiss(exception))
    }
    raise(ApolloOperationError.OperationException(exception))
  }
  if (errors != null) {
    raise(
      ApolloOperationError.OperationError(
        message = errors.map { error ->
          buildString {
            append(error.message)
            if (error.extensions != null) {
              append(error.extensions!!.toList().joinToString(prefix = " ext: [", postfix = "]", separator = ", "))
            }
          }
        }.joinToString(separator = " | "),
      ),
    )
  }
  return requireNotNull(data) {
    "Data must be null after checking all other possible scenarios"
  }
}
