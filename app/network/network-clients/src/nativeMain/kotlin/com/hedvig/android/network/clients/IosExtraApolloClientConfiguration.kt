package com.hedvig.android.network.clients

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.http.DefaultHttpEngine

internal class IosExtraApolloClientConfiguration(
  private val iosAuthTokenInterceptor: IosAuthTokenInterceptor,
) : ExtraApolloClientConfiguration {
  override fun configure(builder: ApolloClient.Builder): ApolloClient.Builder {
    return builder
      .addInterceptor(iosAuthTokenInterceptor)
      .httpEngine(DefaultHttpEngine())
  }
}
