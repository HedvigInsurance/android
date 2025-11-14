package com.hedvig.android.shareddi

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import org.koin.core.module.Module
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val apolloClientBuilderMultiplatformQualifier = qualifier("apolloClientBuilderMultiplatformQualifier")

val sharedModule = module {
  single<NormalizedCacheFactory> {
    MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)
  }
  single<ApolloClient.Builder>(qualifier = apolloClientBuilderMultiplatformQualifier) {
    ApolloClient
      .Builder()
      .normalizedCache(get<NormalizedCacheFactory>())
  }
  single<ApolloClient> {
    val extra = get<ExtraApolloClientConfiguration>()
    get<ApolloClient.Builder>(qualifier = apolloClientBuilderMultiplatformQualifier)
      .copy()
      // todo bring this also from iOS or move outside of android string resources
      .httpServerUrl("https://apollo-router.dev.hedvigit.com")
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
