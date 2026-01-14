package com.hedvig.android.network.clients.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.ktor.ktorClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.baseHttpClientQualifier
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.DeviceIdInterceptor
import com.hedvig.android.network.clients.ExtraApolloClientConfiguration
import com.hedvig.android.network.clients.HedvigHttpLogger
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
import org.koin.dsl.module

val networkModule = module {
  includes(platformNetworkModule)
  single<HttpClient>(baseHttpClientQualifier) {
    buildKtorClient(get<HedvigBuildConstants>(), get<LanguageService>(), get<DeviceIdFetcher>())
  }
  single<HttpClient> {
    get<HttpClient>(baseHttpClientQualifier).config {
      addAuthPlugin(get<AccessTokenFetcher>())
    }
  }
  single<NormalizedCacheFactory> {
    MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)
  }
  single<ApolloClient> {
    val extraConfig = get<ExtraApolloClientConfiguration>()
    ApolloClient.Builder()
      .normalizedCache(get<NormalizedCacheFactory>())
      .ktorClient(get<HttpClient>())
      .httpServerUrl(get<HedvigBuildConstants>().urlGraphqlOctopus)
      .run { extraConfig.configure(this) }
      .build()
  }
}

internal expect val platformNetworkModule: Module

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
        val fetchedToken = accessTokenFetcher.fetch()
        logcat { "loadTokens. fetchedToken: $fetchedToken " }
        val accessToken = fetchedToken ?: return@loadTokens null
        logcat { "loadTokens. accessToken: $accessToken " }
        BearerTokens(accessToken, null)
      }
      refreshTokens {
        val fetchedToken = accessTokenFetcher.fetch()
        logcat { "refreshTokens. fetchedToken: $fetchedToken " }
        val accessToken = fetchedToken ?: return@refreshTokens null
        logcat { "refreshTokens. accessToken: $accessToken " }
        BearerTokens(accessToken, null)
      }
    }
  }
}

internal expect fun HttpClientConfig<*>.installDatadogKtorPlugin(hedvigBuildConstants: HedvigBuildConstants)
