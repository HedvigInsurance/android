package com.hedvig.android.apollo

import arrow.core.Either
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Error as ApolloKotlinError
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloException
import com.hedvig.android.apollo.ApolloOperationError.OperationError
import com.hedvig.android.apollo.ApolloOperationError.OperationException
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

data class GraphqlError(
  val message: String,
  val locations: List<Location>?,
  val paths: List<Path>?,
  val extensions: Extensions,
) {
  data class Location(
    val line: Int,
    val column: Int,
  )

  data class Path(
    val message: String,
  )

  data class Extensions(
    val errorType: ErrorType?,
    val uncategorizedExtensions: Map<String, Any?>?,
  ) {
    @JvmInline
    value class ErrorType(val value: String) {
      companion object {
        val Unauthenticated = ErrorType("UNAUTHENTICATED")
      }
    }
  }
}

fun ApolloKotlinError.toGraphqlError(): GraphqlError {
  return GraphqlError(
    message = this.message,
    locations = this.locations?.map { GraphqlError.Location(it.line, it.column) },
    paths = this.path?.map { GraphqlError.Path(it.toString()) },
    extensions = GraphqlError.Extensions(
      errorType = this.extensions?.get("errorType")?.let { GraphqlError.Extensions.ErrorType(it.toString()) },
      uncategorizedExtensions = this.extensions?.minus("errorType"),
    ),
  )
}

suspend fun <D : Operation.Data> ApolloCall<D>.safeExecute(): Either<ApolloOperationError, D> {
}

suspend fun <D : Operation.Data, E> ApolloCall<D>.safeExecute(mapError: (ApolloOperationError) -> E): Either<E, D> {
}

fun ErrorMessage(apolloOperationError: ApolloOperationError): ErrorMessage = object : ErrorMessage {
  override val message = (apolloOperationError as? OperationError)?.error?.toString()
  override val throwable = (apolloOperationError as? OperationException)?.exception

  override fun toString(): String {
    return "ErrorMessage(message=$message, throwable=$throwable)"
  }
}

sealed interface ApolloOperationError {
  data class OperationError(val error: GraphqlError) : ApolloOperationError

  data class OperationException(val exception: ApolloException) : ApolloOperationError
}

suspend fun <D : Operation.Data> ApolloCall<D>.safeExecute(): OperationResult<D> {
  return try {
    executeV3().toOperationResult()
  } catch (apolloException: ApolloException) {
    OperationResult.Error.NetworkError(apolloException)
  } catch (throwable: Throwable) {
    if (throwable is CancellationException) {
      throw throwable
    }
    OperationResult.Error.GeneralError(throwable)
  }
}

// region asd
fun <D : Operation.Data, ErrorType> ApolloCall<D>.safeFlow(
  ifEmpty: (message: String?, throwable: Throwable?) -> ErrorType,
): Flow<Either<ErrorType, D>> {
  return toFlowV3()
    .map(ApolloResponse<D>::toOperationResult)
    .map { it.toEither(ifEmpty) }
    .catch { throwable ->
      if (throwable is ApolloException) {
        OperationResult.Error.NetworkError(throwable)
      } else {
        OperationResult.Error.GeneralError(throwable)
      }.also {
        emit(it.toEither(ifEmpty))
      }
    }
}

private fun <D : Operation.Data> ApolloResponse<D>.toOperationResult(): OperationResult<D> {
  val data = data
  return when {
    hasErrors() -> {
      val exception = errors?.first()?.extensions?.get("exception")
      val body = (exception as? Map<*, *>)?.get("body")
      val message = (body as? Map<*, *>)?.get("message") as? String

      OperationResult.Error.OperationError(message ?: errors?.first()?.message)
    }

    data != null -> OperationResult.Success(data)
    else -> OperationResult.Error.NoDataError("No data")
  }
}
// endregion
