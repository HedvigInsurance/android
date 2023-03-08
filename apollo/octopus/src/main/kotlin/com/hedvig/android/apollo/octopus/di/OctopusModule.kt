package com.hedvig.android.apollo.octopus.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.common.di.octopusGraphQLUrlQualifier
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val octopusModule = module {
  single<ApolloClient>(octopusClient) {
    get<ApolloClient.Builder>().copy()
      .httpServerUrl(get<String>(octopusGraphQLUrlQualifier))
      .build()
  }
}
