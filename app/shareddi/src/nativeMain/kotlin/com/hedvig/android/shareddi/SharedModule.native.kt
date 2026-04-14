package com.hedvig.android.shareddi

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.hedvig.android.core.common.di.baseHttpClientQualifier
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.network.clients.AccessTokenFetcher
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
}

/**
 * Like [platformModule] but allows for dynamic input, for pieces that need to be injected from iOS
 */
internal fun iosPlatformModule(
  accessTokenFetcher: AccessTokenFetcher,
  deviceIdFetcher: DeviceIdFetcher,
  featureManager: FeatureManager,
) = module {
  single<AccessTokenFetcher> {
    accessTokenFetcher
  }
  single<DeviceIdFetcher> {
    deviceIdFetcher
  }
  single<FeatureManager> {
    featureManager
  }
  single<ImageLoader> {
    ImageLoader.Builder(PlatformContext.INSTANCE)
      .components {
        add(KtorNetworkFetcherFactory(get<HttpClient>(baseHttpClientQualifier)))
      }
      .build()
  }
}
