package com.hedvig.app.util.apollo

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloSubscriptionCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.exception.ApolloException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.Call
import org.json.JSONObject
import ru.gildor.coroutines.okhttp.await
import java.io.IOException

suspend fun <T> ApolloCall<T>.safeQuery(): QueryResult<T> {
    return try {
        await().toQueryResult()
    } catch (apolloException: ApolloException) {
        QueryResult.Error.NetworkError(apolloException.localizedMessage)
    } catch (throwable: Throwable) {
        QueryResult.Error.GeneralError(throwable.localizedMessage)
    }
}

fun <T> ApolloSubscriptionCall<T>.safeSubscription(): Flow<QueryResult<T>> {
    return try {
        toFlow().map(Response<T>::toQueryResult)
    } catch (apolloException: ApolloException) {
        flowOf(QueryResult.Error.NetworkError(apolloException.localizedMessage))
    } catch (throwable: Throwable) {
        flowOf(QueryResult.Error.GeneralError(throwable.localizedMessage))
    }
}

fun <T> Response<T>.toQueryResult(): QueryResult<T> {
    val data = data
    return when {
        hasErrors() -> QueryResult.Error.QueryError(errors?.first()?.message)
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
