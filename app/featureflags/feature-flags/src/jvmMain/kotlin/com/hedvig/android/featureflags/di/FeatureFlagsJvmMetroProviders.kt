package com.hedvig.android.featureflags.di

import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.featureflags.DEVELOPMENT_CLIENT_KEY
import com.hedvig.android.featureflags.JvmUnleashClient
import com.hedvig.android.featureflags.PRODUCTION_CLIENT_KEY
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

@ContributesTo(AppScope::class)
interface FeatureFlagsJvmMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideJvmUnleashClient(
    buildConstants: HedvigBuildConstants,
    applicationScope: ApplicationScope,
    memberIdService: MemberIdService,
  ): JvmUnleashClient {
    return JvmUnleashClient(
      httpClient = HttpClient(OkHttp),
      clientKey = if (buildConstants.isProduction) PRODUCTION_CLIENT_KEY else DEVELOPMENT_CLIENT_KEY,
      appVersionName = buildConstants.appVersionName,
      memberIds = memberIdService.getMemberId(),
      coroutineScope = applicationScope,
    )
  }
}
