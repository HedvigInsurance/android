package com.hedvig.android.featureflags.di

import android.content.Context
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.featureflags.HedvigUnleashClient
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface FeatureFlagsAndroidMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideHedvigUnleashClient(
    applicationContext: Context,
    buildConstants: HedvigBuildConstants,
    applicationScope: ApplicationScope,
    memberIdService: MemberIdService,
  ): HedvigUnleashClient = HedvigUnleashClient(
    androidContext = applicationContext.applicationContext,
    isProduction = buildConstants.isProduction,
    appVersionName = buildConstants.appVersionName,
    coroutineScope = applicationScope,
    memberIdService = memberIdService,
  )
}
