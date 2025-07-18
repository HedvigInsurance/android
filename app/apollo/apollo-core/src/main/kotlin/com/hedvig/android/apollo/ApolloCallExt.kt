package com.hedvig.android.apollo

import arrow.core.Either
import arrow.core.Ior
import arrow.core.IorNel
import arrow.core.Nel
import arrow.core.nel
import arrow.core.raise.IorRaise
import arrow.core.raise.iorNel
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.cache.normalized.watch
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.CacheMissException
import com.hedvig.android.apollo.ApolloOperationError.CacheMiss
import com.hedvig.android.apollo.ApolloOperationError.OperationError
import com.hedvig.android.apollo.ApolloOperationError.OperationException
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

sealed interface ApolloOperationError {
  val throwable: Throwable?
  val containsUnauthenticatedError: Boolean

  data class CacheMiss(override val throwable: CacheMissException) : ApolloOperationError {
    override val containsUnauthenticatedError: Boolean = false

    override fun toString(): String {
      return "CacheMiss(throwableMessage=${throwable.message}, throwable=$throwable)"
    }
  }

  data class OperationException(override val throwable: ApolloException) : ApolloOperationError {
    override val containsUnauthenticatedError: Boolean = false

    override fun toString(): String {
      return "OperationException(throwableMessage=${throwable.message}, throwable=$throwable)"
    }
  }

  sealed interface OperationError : ApolloOperationError {
    object Unathenticated : OperationError {
      override val containsUnauthenticatedError: Boolean = true

      override val throwable: Throwable? = null

      override fun toString(): String {
        return "OperationError.Unathenticated"
      }
    }

    data class Other(
      private val message: String,
      override val containsUnauthenticatedError: Boolean = false,
    ) : OperationError {
      override val throwable: Throwable? = null

      override fun toString(): String {
        return "OperationError.Other(message=$message)"
      }
    }
  }
}

suspend fun <D : Operation.Data> ApolloCall<D>.safeExecute(): Either<ApolloOperationError, D> {
  return iorNel<ApolloOperationError, D> { parseResponse(execute()) }.dropPartialResponses()
}

suspend fun <D : Operation.Data, ErrorType> ApolloCall<D>.safeExecute(
  mapError: (ApolloOperationError) -> ErrorType,
): Either<ErrorType, D> {
  return safeExecute().mapLeft(mapError)
}

fun <D : Operation.Data> ApolloCall<D>.safeFlowAllowingPartialResponses(): Flow<Ior<ApolloOperationError, D>> {
  return internalSafeFlow().map { it.mergeApolloErrors() }
}

fun <D : Operation.Data> ApolloCall<D>.safeFlow(): Flow<Either<ApolloOperationError, D>> {
  return internalSafeFlow().map { it.dropPartialResponses() }
}

private fun <D : Operation.Data> ApolloCall<D>.internalSafeFlow(): Flow<IorNel<ApolloOperationError, D>> {
  return flow<ApolloResponse<D>> {
    var hasEmitted = false
    var errorResponse: ApolloResponse<D>? = null
    toFlow().collect {
      if (it.exception != null) {
        // Some errors may be followed by valid responses.
        // In that case, wait for the next response to come instead.
        errorResponse = it
      } else {
        hasEmitted = true
        emit(it)
      }
    }
    val errorResponseValue = errorResponse
    if (!hasEmitted && errorResponseValue != null) {
      // Flow has terminated without a valid response, emit the error one if it exists.
      emit(errorResponseValue)
    }
  }.map { iorNel { parseResponse(it) } }
}

fun <D : Operation.Data, ErrorType> ApolloCall<D>.safeFlow(
  mapError: (ApolloOperationError) -> ErrorType,
): Flow<Either<ErrorType, D>> {
  return safeFlow().map { it.mapLeft(mapError) }
}

fun <D : Query.Data> ApolloCall<D>.safeWatch(): Flow<Either<ApolloOperationError, D>> {
  return this.watch().map { iorNel { parseResponse(it) }.dropPartialResponses() }
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

@JvmInline
value class ExtensionErrorType(val value: String) {
  companion object {
    val Unauthenticated = ExtensionErrorType("UNAUTHENTICATED")
  }
}

fun Error.extensionErrorType(): ExtensionErrorType? {
  return extensions?.get("errorType")?.let { ExtensionErrorType(it.toString()) }
}

// https://www.apollographql.com/docs/kotlin/essentials/errors/#truth-table
private fun <D : Operation.Data> IorRaise<Nel<ApolloOperationError>>.parseResponse(response: ApolloResponse<D>): D {
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
  return iorFromErrorsAndData(errors.mapToOperationErrors(), data).bind()
}

private fun List<Error>?.mapToOperationErrors(): Nel<ApolloOperationError>? {
  if (this == null) return null
  return map { error ->
    if (error.extensionErrorType() == ExtensionErrorType.Unauthenticated) {
      ApolloOperationError.OperationError.Unathenticated
    } else {
      ApolloOperationError.OperationError.Other(
        buildString {
          append(error.message)
          if (error.extensions != null) {
            append(error.extensions!!.toList().joinToString(prefix = " ext: [", postfix = "]", separator = ", "))
          }
        },
      )
    }
  }.toNonEmptyListOrNull()
}

private fun <D : Operation.Data> IorNel<ApolloOperationError, D>.mergeApolloErrors(): Ior<ApolloOperationError, D> {
  return mapLeft { errors ->
    if (errors.size == 1 && errors.head is ApolloOperationError.OperationError.Unathenticated) {
      errors.head
    } else {
      ApolloOperationError.OperationError.Other(
        message = errors.joinToString(prefix = " [", postfix = "]", separator = ", ") { it.toString() },
        containsUnauthenticatedError = errors.any { it is ApolloOperationError.OperationError.Unathenticated },
      )
    }
  }
}

/**
 * Turn it all back into `Either<ApolloOperationError, D>`, which drops all information where we may have had both
 * errors and data. This only is [Either.Right] if everything went well and we got no responses.
 */
private fun <D : Operation.Data> IorNel<ApolloOperationError, D>.dropPartialResponses():
  Either<ApolloOperationError, D> {
  return mergeApolloErrors().toEither()
}

private fun <D : Operation.Data> iorFromErrorsAndData(
  errors: Nel<ApolloOperationError>?,
  data: D?,
): Ior<Nel<ApolloOperationError>, D> {
  return when {
    errors != null && data != null -> Ior.Both(errors, data)
    errors != null -> Ior.Left(errors)
    data != null -> Ior.Right(data)
    else -> error("Non compliant server")
  }
}

private fun <E> IorRaise<Nel<E>>.raise(value: E): Unit = raise(value.nel())
