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
  private val accessTokenFetcher: AccessTokenFetcher,
) : ApolloInterceptor {
  override fun <D : Operation.Data> intercept(
    request: ApolloRequest<D>,
    chain: ApolloInterceptorChain,
  ): Flow<ApolloResponse<D>> {
    return flow {
      emitAll(
        chain.proceed(
          request
            .newBuilder()
            .run {
              if (accessTokenFetcher.fetch() != null) {
                addHttpHeader("Authorization", "Bearer ${accessTokenFetcher.fetch()}")
              } else {
                this
              }
            }
            .build(),
        ),
      )
    }
  }
}

interface AccessTokenFetcher {
  suspend fun fetch(): String?
}
