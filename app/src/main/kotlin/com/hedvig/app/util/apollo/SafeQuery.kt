package com.hedvig.app.util.apollo

import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.api.Subscription
import com.apollographql.apollo3.cache.normalized.watch
import com.apollographql.apollo3.exception.ApolloException
import com.hedvig.android.core.common.await
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.Call
import org.json.JSONObject
import java.io.IOException

suspend fun <D : Operation.Data> ApolloCall<D>.safeQuery(): QueryResult<D> {
  return try {
    execute().toQueryResult()
  } catch (apolloException: ApolloException) {
    QueryResult.Error.NetworkError(apolloException)
  } catch (throwable: Throwable) {
    if (throwable is CancellationException) {
      throw throwable
    }
    QueryResult.Error.GeneralError(throwable)
  }
}

fun <D : Subscription.Data> ApolloCall<D>.safeSubscription(): Flow<QueryResult<D>> {
  return toFlow()
    .map(ApolloResponse<D>::toQueryResult)
    .catch { exception ->
      if (exception is ApolloException) {
        emit(QueryResult.Error.NetworkError(exception))
      } else {
        emit(QueryResult.Error.GeneralError(exception))
      }
    }
}

/**
 * First tries to fetch the data from the network, and from then on looks into changes of the cache.
 * If the first query from the internet fails, an exception is thrown internally and ends this flow by sending a last
 * value of QueryResult.Error for the consumers to react appropriately.
 * To then retry watching the query, this needs to be started and collected again. This can be done using
 * [com.hedvig.app.util.coroutines.RetryChannel] for example.
 * If any subsequent cache reads fail simply nothing is emitted and the flow continues reading the cache. That is
 * because at that point we do have data to work with so there's no need for the error to propagate.
 */
fun <D : Query.Data> ApolloCall<D>.safeWatch(): Flow<QueryResult<D>> {
  return watch(fetchThrows = true)
    .map(ApolloResponse<D>::toQueryResult)
    .catch { exception ->
      if (exception is ApolloException) {
        emit(QueryResult.Error.NetworkError(exception))
      } else {
        emit(QueryResult.Error.GeneralError(exception))
      }
    }
}

private fun <D : Operation.Data> ApolloResponse<D>.toQueryResult(): QueryResult<D> {
  val data = data
  return when {
    hasErrors() -> {
      val exception = errors?.first()?.extensions?.get("exception")
      val body = (exception as? Map<*, *>)?.get("body")
      val message = (body as? Map<*, *>)?.get("message") as? String
      QueryResult.Error.QueryError(message ?: errors?.first()?.message)
    }
    data != null -> QueryResult.Success(data)
    else -> QueryResult.Error.NoDataError("No data")
  }
}

/**
 * Only to be used when making GraphQL calls.
 * Returns [QueryResult.Success] only when the network request is successful and there are no graphQL error messages.
 * Returns [QueryResult.Error] on all other cases.
 */
@Suppress("BlockingMethodInNonBlockingContext")
suspend fun Call.safeGraphqlCall(): QueryResult<JSONObject> = withContext(Dispatchers.IO) {
  try {
    val response = await()
    if (response.isSuccessful.not()) return@withContext QueryResult.Error.NetworkError(response.message)

    val responseBody = response.body ?: return@withContext QueryResult.Error.NoDataError("No data")
    val jsonObject = JSONObject(responseBody.string())

    val errorJsonObject = jsonObject.optJSONArray("errors")?.getJSONObject(0)
    if (errorJsonObject != null) {
      val errorMessage = errorJsonObject.optString("message")
      return@withContext QueryResult.Error.QueryError(errorMessage)
    }

    QueryResult.Success(jsonObject)
  } catch (ioException: IOException) {
    QueryResult.Error.GeneralError(ioException.localizedMessage)
  }
}
