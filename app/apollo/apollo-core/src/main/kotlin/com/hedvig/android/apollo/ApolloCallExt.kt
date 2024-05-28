package com.hedvig.android.apollo

import arrow.core.Either
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.exception.ApolloException
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
