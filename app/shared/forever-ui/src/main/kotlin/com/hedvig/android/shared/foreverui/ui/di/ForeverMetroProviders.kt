package com.hedvig.android.shared.foreverui.ui.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.shared.foreverui.ui.data.ForeverRepository
import com.hedvig.android.shared.foreverui.ui.data.ForeverRepositoryDemo
import com.hedvig.android.shared.foreverui.ui.data.ForeverRepositoryImpl
import com.hedvig.android.shared.foreverui.ui.data.ForeverRepositoryProvider
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface ForeverMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideForeverRepositoryProvider(
    demoManager: DemoManager,
    prodImpl: ForeverRepositoryImpl,
    demoImpl: ForeverRepositoryDemo,
  ): Provider<ForeverRepository> = ForeverRepositoryProvider(
    demoManager = demoManager,
    demoImpl = demoImpl,
    prodImpl = prodImpl,
  )
}
