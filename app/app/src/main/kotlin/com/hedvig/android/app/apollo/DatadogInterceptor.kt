package com.hedvig.android.app.apollo

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.logError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

class DatadogInterceptor : ApolloInterceptor {
  override fun <D : Operation.Data> intercept(
    request: ApolloRequest<D>,
    chain: ApolloInterceptorChain,
  ): Flow<ApolloResponse<D>> {
    return chain.proceed(request).onEach { response ->
      val errors = response.errors
      if (!errors.isNullOrEmpty() && !isUnauthenticated(errors)) {
        logError(errors, request, response)
      }
    }
  }

  private fun isUnauthenticated(errors: List<Error>) = errors
    .mapNotNull { it.extensions }
    .any { it["errorType"] == "UNAUTHENTICATED" }

  private fun <D : Operation.Data> logError(
    errors: List<Error>,
    request: ApolloRequest<D>,
    response: ApolloResponse<D>,
  ) {
    val exception = errors.first().extensions?.get("exception")
    val body = (exception as? Map<*, *>)?.get("body")
    val message = (body as? Map<*, *>)?.get("message") as? String

    logError(
      message = "GraphQL error for ${request.operation.name()}",
      source = ErrorSource.NETWORK,
      attributes = mapOf(
        "message" to message,
        "body" to body,
        "data" to response.data,
      ),
    )
  }
}
