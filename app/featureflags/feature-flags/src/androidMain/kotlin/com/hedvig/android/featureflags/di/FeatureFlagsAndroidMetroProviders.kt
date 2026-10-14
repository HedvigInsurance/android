package com.hedvig.android.featureflags.di

import android.content.Context
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.featureflags.HedvigUnleashClient
import com.hedvig.android.featureflags.ToggleStateCache
import com.hedvig.android.featureflags.createUnleashConfig
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.getunleash.android.DefaultUnleash

@ContributesTo(AppScope::class)
interface FeatureFlagsAndroidMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideHedvigUnleashClient(
    applicationContext: Context,
    buildConstants: HedvigBuildConstants,
    applicationScope: ApplicationScope,
    memberIdService: MemberIdService,
  ): HedvigUnleashClient {
    val toggleStateCache = ToggleStateCache()
    return HedvigUnleashClient(
      client = DefaultUnleash(
        androidContext = applicationContext.applicationContext,
        unleashConfig = createUnleashConfig(buildConstants.isProduction),
        cacheImpl = toggleStateCache,
      ),
      toggleStateCache = toggleStateCache,
      appVersionName = buildConstants.appVersionName,
      coroutineScope = applicationScope,
      memberIds = memberIdService.getMemberId(),
    )
  }
}
