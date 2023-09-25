package com.hedvig.android.apollo

import arrow.core.Either
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.cache.normalized.watch
import com.apollographql.apollo3.exception.ApolloException
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
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
  }.also { operationResult ->
    if (operationResult is OperationResult.Error) {
      val message: () -> String = {
        "Query:${this.operation.name()} failed with error message: ${operationResult.message}"
      }
      when (operationResult) {
        is OperationResult.Error.GeneralError -> logcat(LogPriority.ERROR, operationResult.throwable, message)
        is OperationResult.Error.NetworkError -> logcat(LogPriority.INFO, operationResult.throwable, message)
        is OperationResult.Error.NoDataError -> logcat(LogPriority.ERROR, operationResult.throwable, message)
        is OperationResult.Error.OperationError -> logcat(LogPriority.ERROR, operationResult.throwable, message)
      }
    }
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

/**
 * First tries to fetch the data from the network, and from then on looks into changes of the cache.
 * If the first query from the internet fails, an exception is thrown internally and ends this flow by sending a last
 * value of QueryResult.Error for the consumers to react appropriately.
 * To then retry watching the query, this needs to be started and collected again. This can be done using
 * [com.hedvig.android.core.common.RetryChannel] for example.
 * If any subsequent cache reads fail simply nothing is emitted and the flow continues reading the cache. That is
 * because at that point we do have data to work with so there's no need for the error to propagate.
 */
fun <D : Query.Data> ApolloCall<D>.safeWatch(): Flow<OperationResult<D>> {
  return watch(fetchThrows = true)
    .map(ApolloResponse<D>::toOperationResult)
    .catch { exception ->
      if (exception is ApolloException) {
        emit(OperationResult.Error.NetworkError(exception))
      } else {
        emit(OperationResult.Error.GeneralError(exception))
      }
    }
}

private fun <D : Operation.Data> ApolloResponse<D>.toOperationResult(): OperationResult<D> {
  val data = data
  return when {
    // TODO here differantiate between unauthorized errors here by looking inside extensions when that's available.
    //  extensions.containsKey("unauthorized") -> OperationResult.Error.OperationError
    hasErrors() -> {
      val exception = errors?.first()?.extensions?.get("exception")
      val body = (exception as? Map<*, *>)?.get("body")
      val message = (body as? Map<*, *>)?.get("message") as? String
      OperationResult.Error.OperationError("message:$message | errors:${errors?.joinToString()} | body:$body")
    }
    data != null -> OperationResult.Success(data)
    else -> OperationResult.Error.NoDataError("No data")
  }
}
