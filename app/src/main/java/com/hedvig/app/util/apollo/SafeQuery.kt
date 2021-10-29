package com.hedvig.app.util.apollo

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import okhttp3.Call
import org.json.JSONObject
import ru.gildor.coroutines.okhttp.await

suspend fun <T> ApolloCall<T>.safeQuery(): QueryResult<T> {
    return try {
        val response = await()
        val data = response.data
        when {
            response.hasErrors() -> QueryResult.Error.QueryError(response.errors?.first()?.message)
            data != null -> QueryResult.Success(data)
            else -> QueryResult.Error.NoDataError("No data")
        }
    } catch (apolloException: ApolloException) {
        QueryResult.Error.NetworkError(apolloException.localizedMessage)
    } catch (throwable: Throwable) {
        QueryResult.Error.GeneralError(throwable.localizedMessage)
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

sealed class QueryResult<T> {
    data class Success<T>(val data: T) : QueryResult<T>()
    sealed class Error<T> : QueryResult<T>() {

        abstract val message: String?

        data class NoDataError<T>(override val message: String?) : Error<T>()
        data class GeneralError<T>(override val message: String?) : Error<T>()
        data class QueryError<T>(override val message: String?) : Error<T>()
        data class NetworkError<T>(override val message: String?) : Error<T>()
    }
}
