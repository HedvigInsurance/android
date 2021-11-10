package com.hedvig.app.util.apollo

import com.apollographql.apollo.api.ApolloExperimental
import com.apollographql.apollo.api.Response

@OptIn(ApolloExperimental::class)
fun <From, To> Response<From>.map(mappingFunction: (From) -> To): Response<To> {
    val data = this.data
    val mappedData = if (data != null) mappingFunction(data) else data
    return Response(
        operation = this.operation,
        data = mappedData,
        errors = this.errors,
        dependentKeys = this.dependentKeys,
        isFromCache = this.isFromCache,
        extensions = this.extensions,
        executionContext = this.executionContext,
    )
}

fun <From, To> QueryResult<From>.map(mappingFunction: (From) -> To): QueryResult<To> {
    return when (this) {
        is QueryResult.Error.GeneralError -> QueryResult.Error.GeneralError(this.message)
        is QueryResult.Error.NetworkError -> QueryResult.Error.NetworkError(this.message)
        is QueryResult.Error.NoDataError -> QueryResult.Error.NoDataError(this.message)
        is QueryResult.Error.QueryError -> QueryResult.Error.QueryError(this.message)
        is QueryResult.Success -> QueryResult.Success(mappingFunction(this.data))
    }
}
