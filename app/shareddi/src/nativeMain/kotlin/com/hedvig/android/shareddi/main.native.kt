package com.hedvig.android.shareddi

import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.feature.help.center.di.helpCenterModule
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.feature.claim.chat.di.claimChatModule
import org.koin.core.context.startKoin

@Suppress("unused") // Used from iOS
fun initKoin(
  accessTokenFetcher: AccessTokenFetcher,
  deviceIdFetcher: DeviceIdFetcher,
  featureManager: FeatureManager,
  appBuildConfig: AppBuildConfig,
) {
  startKoin {
    modules(
      iosPlatformModule(accessTokenFetcher, deviceIdFetcher, featureManager),
      sharedModule(appBuildConfig),
      claimChatModule,
      helpCenterModule,
    )
  }
}
