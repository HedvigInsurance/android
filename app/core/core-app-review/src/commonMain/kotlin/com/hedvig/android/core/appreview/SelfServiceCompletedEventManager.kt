package com.hedvig.android.core.appreview

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

interface SelfServiceCompletedEventManager {
  suspend fun completedSelfServiceSuccessfully()

  suspend fun resetSelfServiceCompletions()
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class SelfServiceCompletedEventManagerImpl(
  private val dataStore: SelfServiceCompletedEventStore,
) : SelfServiceCompletedEventManager {
  override suspend fun completedSelfServiceSuccessfully() {
    dataStore.onSelfServiceCompleted()
  }

  override suspend fun resetSelfServiceCompletions() {
    dataStore.resetSelfServiceCompletions()
  }
}
