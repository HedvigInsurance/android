package com.hedvig.android.apollo.giraffe.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.common.di.giraffeGraphQLUrlQualifier
import com.hedvig.android.core.common.di.giraffeGraphQLWebSocketUrlQualifier
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val giraffeModule = module {
  single<ApolloClient>(giraffeClient) {
    get<ApolloClient.Builder>().copy()
      .httpServerUrl(get<String>(giraffeGraphQLUrlQualifier))
      .webSocketServerUrl(get<String>(giraffeGraphQLWebSocketUrlQualifier))
      .build()
  }
}
