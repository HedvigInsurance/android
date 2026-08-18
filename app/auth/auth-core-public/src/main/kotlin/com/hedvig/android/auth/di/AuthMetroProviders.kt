package com.hedvig.android.auth.di

import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.authlib.AuthEnvironment
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.NetworkAuthRepository
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface AuthMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideAuthRepository(hedvigBuildConstants: HedvigBuildConstants): AuthRepository = NetworkAuthRepository(
    environment = if (hedvigBuildConstants.isProduction) {
      AuthEnvironment.PRODUCTION
    } else {
      AuthEnvironment.STAGING
    },
    additionalHttpHeadersProvider = { emptyMap() },
  )
}
