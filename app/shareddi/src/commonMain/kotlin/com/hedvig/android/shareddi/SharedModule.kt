package com.hedvig.android.shareddi

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.ktor.ktorClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.baseHttpClientQualifier
import com.hedvig.android.language.LanguageService
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.headers
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
  single<HttpClient>(baseHttpClientQualifier) {
    buildKtorClient(get<HedvigBuildConstants>(), get<LanguageService>(), get<DeviceIdFetcher>())
  }
  single<HttpClient> {
    get<HttpClient>(baseHttpClientQualifier).config {
      addAuthPlugin(get<AccessTokenFetcher>())
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

private fun buildKtorClient(
  hedvigBuildConstants: HedvigBuildConstants,
  languageService: LanguageService,
  deviceIdFetcher: DeviceIdFetcher,
): HttpClient {
  return HttpClient {
    installDatadogKtorPlugin(hedvigBuildConstants)
    defaultRequest {
      commonHeaders(hedvigBuildConstants, languageService)
    }
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

private fun DefaultRequest.DefaultRequestBuilder.commonHeaders(
  hedvigBuildConstants: HedvigBuildConstants,
  languageService: LanguageService,
) {
  headers {
    append("User-Agent", hedvigBuildConstants.userAgent)
    append("Accept-Language", languageService.getLanguage().toBcp47Format())
    append("hedvig-language", languageService.getLanguage().toBcp47Format())
    append("apollographql-client-name", hedvigBuildConstants.appPackageId)
    append("apollographql-client-version", hedvigBuildConstants.appVersionName)
    append("X-Build-Version", hedvigBuildConstants.appVersionCode)
    append("X-App-Version", hedvigBuildConstants.appVersionName)
    append("X-System-Version", hedvigBuildConstants.buildApiVersion.toString())
    append("X-Platform", hedvigBuildConstants.platformName)
    append("X-Model", hedvigBuildConstants.model)
    append("Hedvig-App-Version", "android;${hedvigBuildConstants.appVersionName}")
  }
}

private fun HttpClientConfig<*>.addAuthPlugin(
  accessTokenFetcher: AccessTokenFetcher,
) {
  install(Auth) {
    bearer {
      loadTokens {
        val accessToken = accessTokenFetcher.fetch() ?: return@loadTokens null
        BearerTokens(accessToken, null)
      }
    }
  }
}
