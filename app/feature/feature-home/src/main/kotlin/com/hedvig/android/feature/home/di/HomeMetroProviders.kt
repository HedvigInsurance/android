package com.hedvig.android.feature.home.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseDemo
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseImpl
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface HomeMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideGetHomeDataUseCaseProvider(
    demoManager: DemoManager,
    prodImpl: GetHomeDataUseCaseImpl,
    demoImpl: GetHomeDataUseCaseDemo,
  ): Provider<GetHomeDataUseCase> = GetHomeDataUseCaseProvider(
    demoManager = demoManager,
    demoImpl = demoImpl,
    prodImpl = prodImpl,
  )
}
