package com.hedvig.app.util.apollo

import com.apollographql.apollo3.api.ApolloRequest
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.interceptor.ApolloInterceptor
import com.apollographql.apollo3.interceptor.ApolloInterceptorChain
import com.datadog.android.rum.GlobalRumMonitor
import com.datadog.android.rum.RumErrorSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DatadogInterceptor : ApolloInterceptor {
  override fun <D : Operation.Data> intercept(
    request: ApolloRequest<D>,
    chain: ApolloInterceptorChain,
  ): Flow<ApolloResponse<D>> {
    return chain.proceed(request)
      .map { response ->
        val errors = response.errors
        if (!errors.isNullOrEmpty()) {
          GlobalRumMonitor.get().addError(
            message = ERROR_MSG_FORMAT.format(request.operation.name(), errors.map { it.message }),
            source = RumErrorSource.NETWORK,
            throwable = null,
            attributes = mapOf(),
          )
        }
        response
      }
  }

  companion object {
    private const val ERROR_MSG_FORMAT = "Apollo GraphQL operation error %s, errors: %s"
  }
}
