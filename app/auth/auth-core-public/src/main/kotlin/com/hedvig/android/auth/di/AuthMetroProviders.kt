package com.hedvig.android.auth.di

import com.hedvig.android.authlib.AuthEnvironment
import com.hedvig.android.authlib.AuthRepository
import com.hedvig.android.authlib.networkAuthRepositoryWithEngine
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.AuthHttpClientEngine
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.engine.HttpClientEngine

@ContributesTo(AppScope::class)
interface AuthMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideAuthRepository(
    hedvigBuildConstants: HedvigBuildConstants,
    @AuthHttpClientEngine engine: HttpClientEngine,
  ): AuthRepository = networkAuthRepositoryWithEngine(
    environment = if (hedvigBuildConstants.isProduction) {
      AuthEnvironment.PRODUCTION
    } else {
      AuthEnvironment.STAGING
    },
    additionalHttpHeadersProvider = { emptyMap() },
    engine = engine,
  )
}
