package com.hedvig.android.shareddi

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import org.koin.core.module.Module
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val apolloClientBuilderMultiplatformQualifier = qualifier("apolloClientBuilderMultiplatformQualifier")

val sharedModule = module {
  includes(platformModule)
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
  single<HttpClient> {
    ktorClient(get<AccessTokenFetcher>())
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

private fun ktorClient(accessTokenFetcher: AccessTokenFetcher): HttpClient {
  return HttpClient {
    install(Auth) {
      bearer {
        loadTokens {
          val accessToken = accessTokenFetcher.fetch() ?: return@loadTokens null
          BearerTokens(accessToken, null)
        }
      }
    }
  }
}
