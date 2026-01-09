package com.hedvig.android.network.clients.di

import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.ExtraApolloClientConfiguration
import com.hedvig.android.network.clients.NoopExtraApolloClientConfiguration
import io.ktor.client.HttpClientConfig
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformNetworkModule: Module = module {
  single<AccessTokenFetcher> {
    object : AccessTokenFetcher {
      override suspend fun fetch(): String? {
        return null
      }
    }
  }
  single<ExtraApolloClientConfiguration> {
    NoopExtraApolloClientConfiguration()
  }
}

internal actual fun HttpClientConfig<*>.installDatadogKtorPlugin(hedvigBuildConstants: HedvigBuildConstants) {
  // no-op
}
