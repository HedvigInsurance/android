package com.hedvig.android.data.addons.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.addons.data.DemoGetAddonBannerInfoUseCase
import com.hedvig.android.data.addons.data.GetAddonBannerInfoUseCase
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCaseProvider
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface DataAddonsMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideGetTravelAddonBannerInfoUseCaseProvider(
    demoManager: DemoManager,
    prodImpl: GetAddonBannerInfoUseCase,
    demoImpl: DemoGetAddonBannerInfoUseCase,
  ): GetTravelAddonBannerInfoUseCaseProvider = GetTravelAddonBannerInfoUseCaseProvider(
    demoManager = demoManager,
    demoImpl = demoImpl,
    prodImpl = prodImpl,
  )
}
