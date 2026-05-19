package com.hedvig.android.shareddi

import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.di.buildConstantsModule
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.core.datastore.di.dataStoreModule
import com.hedvig.android.data.conversations.di.dataConversationsModule
import com.hedvig.android.feature.help.center.di.helpCenterModule
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.language.LanguageStorage
import com.hedvig.android.language.di.languageModule
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.permission.di.noopPermissionModule
import com.hedvig.feature.claim.chat.di.claimChatModule
import org.koin.core.context.startKoin

@Suppress("unused") // Used from iOS
fun initKoin(
  accessTokenFetcher: AccessTokenFetcher,
  deviceIdFetcher: DeviceIdFetcher,
  featureManager: FeatureManager,
  languageStorage: LanguageStorage,
  appBuildConfig: AppBuildConfig,
) {
  startKoin {
    modules(
      iosPlatformModule(accessTokenFetcher, deviceIdFetcher, featureManager, languageStorage),
      sharedModule(appBuildConfig),
      dataStoreModule,
      languageModule,
      dataConversationsModule,
      claimChatModule,
      helpCenterModule,
      noopPermissionModule,
    )
  }
}
