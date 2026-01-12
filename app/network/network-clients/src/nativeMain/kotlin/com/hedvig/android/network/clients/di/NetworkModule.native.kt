package com.hedvig.android.network.clients.di

import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.ExtraApolloClientConfiguration
import com.hedvig.android.network.clients.IosAuthTokenInterceptor
import com.hedvig.android.network.clients.IosExtraApolloClientConfiguration
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformNetworkModule: Module = module {
  single<ExtraApolloClientConfiguration> {
    IosExtraApolloClientConfiguration(get<IosAuthTokenInterceptor>())
  }
  single<IosAuthTokenInterceptor> {
    IosAuthTokenInterceptor(get<AccessTokenFetcher>())
  }
}
