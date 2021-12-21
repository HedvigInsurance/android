package com.hedvig.app.util.apollo

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloSubscriptionCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.exception.ApolloException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import okhttp3.Call
import org.json.JSONObject
import ru.gildor.coroutines.okhttp.await

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

suspend fun Call.safeCall(): QueryResult<JSONObject> {
    return try {
        val response = await()
        when {
            response.isSuccessful -> {
                val json = response.body?.string()?.let { JSONObject(it) }
                if (json == null) {
                    QueryResult.Error.NoDataError("No data")
                } else {
                    QueryResult.Success(json)
                }
            }
            else -> QueryResult.Error.NetworkError(response.message)
        }
    } catch (throwable: Throwable) {
        QueryResult.Error.GeneralError(throwable.localizedMessage)
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
}
