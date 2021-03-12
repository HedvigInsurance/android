package com.hedvig.app.util.apollo

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException

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

sealed class QueryResult<T> {
    data class Success<T>(val data: T): QueryResult<T>()
    sealed class Error<T> : QueryResult<T>() {

        abstract val message: String?

        data class NoDataError<T>(override val message: String?) : Error<T>()
        data class GeneralError<T>(override val message: String?) : Error<T>()
        data class QueryError<T>(override val message: String?) : Error<T>()
        data class NetworkError<T>(override val message: String?) : Error<T>()
    }
}
