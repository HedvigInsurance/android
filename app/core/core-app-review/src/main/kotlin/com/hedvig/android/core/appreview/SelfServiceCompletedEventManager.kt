package com.hedvig.android.core.appreview

interface SelfServiceCompletedEventManager {
  suspend fun completedSelfServiceSuccessfully()

  suspend fun resetSelfServiceCompletions()
}

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
