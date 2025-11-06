package com.hedvig.android.shareddi

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import kotlinx.coroutines.flow.Flow

internal class IosAuthTokenInterceptorImpl(
  private val getAuthToken: () -> String,
) : IosAuthTokenInterceptor {
  override fun <D : Operation.Data> intercept(
    request: ApolloRequest<D>,
    chain: ApolloInterceptorChain,
  ): Flow<ApolloResponse<D>> {
    return chain.proceed(
      request
        .newBuilder()
        .addHttpHeader("Authorization", "Bearer ${getAuthToken()}")
        .build()
    )
  }
}
