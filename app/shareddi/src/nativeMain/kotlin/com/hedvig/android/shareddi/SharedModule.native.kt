package com.hedvig.android.shareddi

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.http.DefaultHttpEngine
import com.hedvig.android.core.datastore.DeviceIdFetcher
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
  single<ExtraApolloClientConfiguration> {
    IosExtraApolloClientConfiguration(get<IosAuthTokenInterceptor>())
  }
}

/**
 * Like [platformModule] but allows for dynamic input, for pieces that need to be injected from iOS
 */
internal fun iosPlatformModule(
  accessTokenFetcher: AccessTokenFetcher,
  deviceIdFetcher: DeviceIdFetcher,
) = module {
  single<AccessTokenFetcher> {
    accessTokenFetcher
  }
  single<IosAuthTokenInterceptor> {
    IosAuthTokenInterceptor(get<AccessTokenFetcher>())
  }
  single<DeviceIdFetcher> {
    deviceIdFetcher
  }
}

private class IosExtraApolloClientConfiguration(
  private val iosAuthTokenInterceptor: IosAuthTokenInterceptor,
) : ExtraApolloClientConfiguration {
  override fun configure(builder: ApolloClient.Builder): ApolloClient.Builder {
    return builder
      .addInterceptor(iosAuthTokenInterceptor)
      .httpEngine(DefaultHttpEngine())
  }
}
