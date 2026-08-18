package com.hedvig.android.core.common.di

import com.hedvig.android.core.common.ApplicationScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@ContributesTo(AppScope::class)
interface CoreCommonMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideApplicationScope(): ApplicationScope = ApplicationScope()

  @Provides
  @SingleIn(AppScope::class)
  @IoDispatcher
  fun provideIoCoroutineContext(): CoroutineContext = Dispatchers.IO
}
