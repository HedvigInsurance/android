package com.hedvig.android.apollo

import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.api.Subscription
import com.apollographql.apollo3.cache.normalized.watch
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

fun <D : Subscription.Data> ApolloCall<D>.toSafeFlow(): Flow<OperationResult<D>> {
  return toFlow()
    .map(ApolloResponse<D>::toOperationResult)
    .catch { exception ->
      if (exception is ApolloException) {
        emit(OperationResult.Error.NetworkError(exception))
      } else {
        emit(OperationResult.Error.GeneralError(exception))
      }
    }
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
