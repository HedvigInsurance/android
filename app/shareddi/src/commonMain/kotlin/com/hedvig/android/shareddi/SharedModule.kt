package com.hedvig.android.shareddi

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import org.koin.core.module.Module
import org.koin.dsl.module

typealias ApolloClientBuilderMultiplatform = ApolloClient.Builder

val sharedModule = module {
  single<NormalizedCacheFactory> {
    MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)
  }
  single<ApolloClientBuilderMultiplatform> {
    ApolloClient
      .Builder()
      .normalizedCache(get<NormalizedCacheFactory>())
  }
  single<ApolloClient> {
    val extra = get<ExtraApolloClientConfiguration>()
    get<ApolloClient.Builder>()
      .copy()
      .httpServerUrl(get<HedvigBuildConstants>().urlGraphqlOctopus)
      .run { extra.configure(this) }
      .build()
  }
}

internal expect val platformModule: Module

internal interface ExtraApolloClientConfiguration {
  fun configure(builder: ApolloClient.Builder): ApolloClient.Builder
}

internal class NoopExtraApolloClientConfiguration : ExtraApolloClientConfiguration {
  override fun configure(builder: ApolloClient.Builder): ApolloClient.Builder {
    return builder
  }
}
