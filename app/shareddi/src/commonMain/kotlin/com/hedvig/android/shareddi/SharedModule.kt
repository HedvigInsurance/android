package com.hedvig.android.shareddi

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.ktor.ktorClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.baseHttpClientQualifier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
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
      .ktorClient(get<HttpClient>())
      .run { extra.configure(this) }
      .build()
  }
  single<HttpClient> {
    get<HttpClient>(baseHttpClientQualifier).config {
      ktorClient(get<AccessTokenFetcher>(), get<HedvigBuildConstants>())
    }
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

internal expect fun HttpClientConfig<*>.installDatadogKtorPlugin(hedvigBuildConstants: HedvigBuildConstants)

private fun HttpClientConfig<*>.ktorClient(
  accessTokenFetcher: AccessTokenFetcher,
private fun buildKtorClient(
  hedvigBuildConstants: HedvigBuildConstants,
  languageService: LanguageService,
  deviceIdFetcher: DeviceIdFetcher,
): HttpClient {
  return HttpClient {
    if (!hedvigBuildConstants.isProduction) {
      Logging {
        level = LogLevel.BODY
        logger = HedvigHttpLogger()
      }
    }
    install(HttpSend)
  }.apply {
    plugin(HttpSend).intercept(DeviceIdInterceptor(deviceIdFetcher))
  }
}

  hedvigBuildConstants: HedvigBuildConstants,
) {
  install(Auth) {
    bearer {
      loadTokens {
        val accessToken = accessTokenFetcher.fetch() ?: return@loadTokens null
        BearerTokens(accessToken, null)
      }
    }
  }
  install(
    datadogKtorPlugin(
      tracedHosts = mapOf(
        hedvigBuildConstants.urlGraphqlOctopus.removePrefix("""https://""") to setOf(TracingHeaderType.DATADOG),
      ),
      traceSampleRate = 100f
    )
  )
}
