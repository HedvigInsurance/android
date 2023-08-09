package com.hedvig.android.apollo.octopus.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val octopusModule = module {
  single<ApolloClient>(octopusClient) {
    get<ApolloClient.Builder>().copy()
      .httpServerUrl(get<HedvigBuildConstants>().urlGraphqlOctopus)
      .build()
  }
}
