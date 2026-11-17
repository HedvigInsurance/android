package com.hedvig.android.umbrella

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock

@ContributesTo(AppScope::class)
internal interface IosMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideKotlinClock(): Clock = Clock.System
}
