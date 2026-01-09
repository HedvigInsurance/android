package com.hedvig.android.network.clients.di

import com.hedvig.android.auth.AccessTokenProvider
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.ExtraApolloClientConfiguration
import com.hedvig.android.network.clients.NoopExtraApolloClientConfiguration
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformNetworkModule: Module = module {
  single<AccessTokenFetcher> {
    val provider = get<AccessTokenProvider>()
    object : AccessTokenFetcher {
      override suspend fun fetch(): String? {
        return provider.provide()
      }
    }
  }
}
