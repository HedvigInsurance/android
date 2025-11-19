package com.hedvig.android.shareddi

import com.hedvig.android.auth.AccessTokenProvider
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
  single<AccessTokenFetcher> {
    val provider = get<AccessTokenProvider>()
    object : AccessTokenFetcher {
      override suspend fun fetch(): String? {
        return provider.provide()
      }
    }
  }
  single<ExtraApolloClientConfiguration> {
    NoopExtraApolloClientConfiguration()
  }
}
