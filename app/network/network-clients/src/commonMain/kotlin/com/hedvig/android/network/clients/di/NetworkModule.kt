package com.hedvig.android.network.clients.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.ktor.ktorClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.DeviceIdInterceptor
import com.hedvig.android.network.clients.ExtraApolloClientConfiguration
import com.hedvig.android.network.clients.HedvigHttpLogger
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpSendInterceptor
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.client.request.headers

@ContributesTo(AppScope::class)
interface NetworkMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  @BaseHttpClient
  fun provideBaseHttpClient(
    hedvigBuildConstants: HedvigBuildConstants,
    languageService: LanguageService,
    deviceIdFetcher: DeviceIdFetcher,
  ): HttpClient = buildKtorClient(hedvigBuildConstants, languageService, deviceIdFetcher)

  @Provides
  @SingleIn(AppScope::class)
  fun provideHttpClient(
    @BaseHttpClient baseHttpClient: HttpClient,
    accessTokenFetcher: AccessTokenFetcher,
  ): HttpClient = baseHttpClient
    .config {}
    .apply { addAuthPlugin(accessTokenFetcher) }

  @Provides
  @SingleIn(AppScope::class)
  fun provideNormalizedCacheFactory(): NormalizedCacheFactory = MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)

  @Provides
  @SingleIn(AppScope::class)
  fun provideApolloClient(
    extraApolloClientConfiguration: ExtraApolloClientConfiguration,
    normalizedCacheFactory: NormalizedCacheFactory,
    httpClient: HttpClient,
    hedvigBuildConstants: HedvigBuildConstants,
  ): ApolloClient = ApolloClient.Builder()
    .normalizedCache(normalizedCacheFactory)
    .ktorClient(httpClient)
    .httpServerUrl(hedvigBuildConstants.urlGraphqlOctopus)
    .run { extraApolloClientConfiguration.configure(this) }
    .build()
}

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
    append(
      "Hedvig-App-Version",
      "${hedvigBuildConstants.platformName.lowercase()};${hedvigBuildConstants.appVersionName}",
    )
  }
}

private fun HttpClient.addAuthPlugin(accessTokenFetcher: AccessTokenFetcher) {
  plugin(HttpSend).intercept { request ->
    val accessToken = accessTokenFetcher.fetch()
    execute(
      if (accessToken != null) {
        request.apply { header("Authorization", "Bearer ${accessTokenFetcher.fetch()}") }
      } else {
        request
      },
    )
  }
}

internal expect fun HttpClientConfig<*>.installDatadogKtorPlugin(hedvigBuildConstants: HedvigBuildConstants)
