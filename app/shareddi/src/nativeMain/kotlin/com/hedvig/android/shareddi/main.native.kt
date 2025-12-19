package com.hedvig.android.shareddi

import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.feature.claim.chat.di.claimChatModule
import org.koin.core.context.startKoin

@Suppress("unused") // Used from iOS
fun initKoin(
  accessTokenFetcher: AccessTokenFetcher,
  deviceIdFetcher: DeviceIdFetcher,
  appBuildConfig: AppBuildConfig,
) {
  startKoin {
    modules(iosPlatformModule(accessTokenFetcher, deviceIdFetcher), sharedModule(appBuildConfig), claimChatModule)
  }
}
