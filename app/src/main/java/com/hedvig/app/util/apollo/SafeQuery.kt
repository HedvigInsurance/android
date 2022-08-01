package com.hedvig.app.util.apollo

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.Call
import org.json.JSONObject
import java.io.IOException

suspend fun <D : Operation.Data> ApolloCall<D>.safeQuery(): QueryResult<D> {
  return try {
    execute().toQueryResult()
  } catch (apolloException: ApolloException) {
    QueryResult.Error.NetworkError(apolloException.localizedMessage)
  } catch (throwable: Throwable) {
    if (throwable is CancellationException) {
      throw throwable
    }
    QueryResult.Error.GeneralError(throwable.localizedMessage)
  }
}

fun <D : Subscription.Data> ApolloCall<D>.safeSubscription(): Flow<QueryResult<D>> {
  return try {
    toFlow().map(ApolloResponse<D>::toQueryResult)
  } catch (apolloException: ApolloException) {
    flowOf(QueryResult.Error.NetworkError(apolloException.localizedMessage))
  } catch (throwable: Throwable) {
    if (throwable is CancellationException) {
      throw throwable
    }
    flowOf(QueryResult.Error.GeneralError(throwable.localizedMessage))
  }
}

fun <D : Query.Data> ApolloCall<D>.safeWatch(): Flow<QueryResult<D>> {
  return try {
    watch(null)
    watch().map(ApolloResponse<D>::toQueryResult)
  } catch (apolloException: ApolloException) {
    flowOf(QueryResult.Error.NetworkError(apolloException.localizedMessage))
  } catch (throwable: Throwable) {
    if (throwable is CancellationException) {
      throw throwable
    }
    flowOf(QueryResult.Error.GeneralError(throwable.localizedMessage))
  }
}

private fun <D : Operation.Data> ApolloResponse<D>.toQueryResult(): QueryResult<D> {
  val data = data
  return when {
    hasErrors() -> {
      val exception1 = errors?.first()?.extensions?.get("exception")
      val body = (exception1 as? Map<*, *>)?.get("body")
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

sealed class QueryResult<out T> {
  data class Success<T>(val data: T) : QueryResult<T>()
  sealed class Error : QueryResult<Nothing>() {

    abstract val message: String?

    data class NoDataError(override val message: String?) : Error()
    data class GeneralError(override val message: String?) : Error()
    data class QueryError(override val message: String?) : Error()
    data class NetworkError(override val message: String?) : Error()
  }

  fun toOption(): Option<T> = when (this) {
    is Error -> None
    is Success -> Some(this.data)
  }

  fun toEither(): Either<Error, T> = when (this) {
    is Error -> Either.Left(this)
    is Success -> Either.Right(this.data)
  }

  inline fun <ErrorType> toEither(ifEmpty: (message: String?) -> ErrorType): Either<ErrorType, T> = when (this) {
    is Error -> Either.Left(ifEmpty(message))
    is Success -> Either.Right(this.data)
  }
}
