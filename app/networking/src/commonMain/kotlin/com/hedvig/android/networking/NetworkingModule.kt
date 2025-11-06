package com.hedvig.android.networking

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import org.koin.dsl.module

typealias ApolloClientBuilderMultiplatform = ApolloClient.Builder

val networkingModule = module {
  single<NormalizedCacheFactory> {
    MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)
  }
  single<ApolloClientBuilderMultiplatform> {
    ApolloClient
      .Builder()
      .normalizedCache(get<NormalizedCacheFactory>())
  }
  single<ApolloClient> {
    get<ApolloClient.Builder>()
      .copy()
      .httpServerUrl(get<HedvigBuildConstants>().urlGraphqlOctopus)
      .build()
  }
}
