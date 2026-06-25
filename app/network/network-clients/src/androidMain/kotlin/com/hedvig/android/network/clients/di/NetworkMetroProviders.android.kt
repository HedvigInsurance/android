package com.hedvig.android.network.clients.di

import com.hedvig.android.auth.AccessTokenProvider
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.network.clients.AccessTokenFetcher
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface AndroidNetworkMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideAccessTokenFetcher(accessTokenProvider: AccessTokenProvider): AccessTokenFetcher =
    object : AccessTokenFetcher {
      override suspend fun fetch(): String? = accessTokenProvider.provide()
    }
}
