package com.hedvig.android.apollo

import arrow.core.Either
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.exception.ApolloException
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.logError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

suspend fun <D : Operation.Data> ApolloCall<D>.safeExecute(): OperationResult<D> {
  return try {
    execute().toOperationResult()
  } catch (apolloException: ApolloException) {
    OperationResult.Error.NetworkError(apolloException)
  } catch (throwable: Throwable) {
    if (throwable is CancellationException) {
      throw throwable
    }
    OperationResult.Error.GeneralError(throwable)
  }
}

fun <D : Operation.Data, ErrorType> ApolloCall<D>.safeFlow(
  ifEmpty: (message: String?, throwable: Throwable?) -> ErrorType,
): Flow<Either<ErrorType, D>> {
  return toFlow()
    .map(ApolloResponse<D>::toOperationResult)
    .catch { throwable ->
      if (throwable is ApolloException) {
        OperationResult.Error.NetworkError(throwable)
      } else {
        OperationResult.Error.GeneralError(throwable)
      }
    }
    .map { it.toEither(ifEmpty) }
}

private fun <D : Operation.Data> ApolloResponse<D>.toOperationResult(): OperationResult<D> {
  val data = data
  return when {
    hasErrors() -> {
      val exception = errors?.first()?.extensions?.get("exception")

      val hasUnauthenticatedErrors = errors
        ?.mapNotNull { it.extensions }
        ?.any { it["errorType"] == "UNAUTHENTICATED" }

      val body = (exception as? Map<*, *>)?.get("body")
      val message = (body as? Map<*, *>)?.get("message") as? String

      if (hasUnauthenticatedErrors != true) {
        logError(
          message = "GraphQL error for ${operation.name()}",
          source = ErrorSource.NETWORK,
          attributes = mapOf(
            "message" to message,
            "body" to body,
            "data" to data,
          ),
        )
      }

      OperationResult.Error.OperationError(message ?: errors?.first()?.message)
    }

    data != null -> OperationResult.Success(data)
    else -> {
      logError(
        message = "GraphQL empty response for ${operation.name()}",
        source = ErrorSource.NETWORK,
        attributes = mapOf(),
      )

      OperationResult.Error.NoDataError("No data")
    }
  }
}
