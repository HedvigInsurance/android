package com.hedvig.android.shareddi

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

internal class IosAuthTokenInterceptor(
//  private val getAuthToken: () -> String,
  private val accessTokenFetcher: AccessTokenFetcher,
) : ApolloInterceptor {
  override fun <D : Operation.Data> intercept(
    request: ApolloRequest<D>,
    chain: ApolloInterceptorChain,
  ): Flow<ApolloResponse<D>> {
    return flow {
      var accessToken: String? = null
      accessTokenFetcher.fetch { accessToken = it }
      emitAll(
        chain.proceed(
          request
            .newBuilder()
            .addHttpHeader("Authorization", "Bearer ${accessToken!!}")
            .build(),
        ),
      )
    }
  }
}

interface AccessTokenFetcher {
  fun fetch(completionHandler: (String) -> Unit)
}
