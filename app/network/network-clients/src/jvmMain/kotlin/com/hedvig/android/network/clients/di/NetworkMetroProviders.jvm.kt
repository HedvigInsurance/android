package com.hedvig.android.network.clients.di

import com.hedvig.android.auth.AccessTokenProvider
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.AuthHttpClientEngine
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.ExtraApolloClientConfiguration
import com.hedvig.android.network.clients.ExtraKtorClientConfiguration
import com.hedvig.android.network.clients.NoopExtraApolloClientConfiguration
import com.hedvig.android.network.clients.NoopExtraKtorClientConfiguration
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

@ContributesTo(AppScope::class)
interface JvmNetworkMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideAccessTokenFetcher(accessTokenProvider: AccessTokenProvider): AccessTokenFetcher =
    object : AccessTokenFetcher {
      override suspend fun fetch(): String? = accessTokenProvider.provide()
    }

  @Provides
  @SingleIn(AppScope::class)
  fun provideExtraApolloClientConfiguration(): ExtraApolloClientConfiguration = NoopExtraApolloClientConfiguration()

  @Provides
  @SingleIn(AppScope::class)
  fun provideExtraKtorClientConfiguration(): ExtraKtorClientConfiguration = NoopExtraKtorClientConfiguration()

  @Provides
  @SingleIn(AppScope::class)
  @AuthHttpClientEngine
  fun provideAuthHttpClientEngine(): HttpClientEngine = httpClientEngineFactory().create()
}

internal actual fun HttpClientConfig<*>.installDatadogKtorPlugin(hedvigBuildConstants: HedvigBuildConstants) {
  // no-op
}

internal actual fun httpClientEngineFactory(): HttpClientEngineFactory<*> = OkHttp
