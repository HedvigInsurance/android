package com.hedvig.android.shareddi

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.http.DefaultHttpEngine
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.network.clients.AccessTokenFetcher
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
}
