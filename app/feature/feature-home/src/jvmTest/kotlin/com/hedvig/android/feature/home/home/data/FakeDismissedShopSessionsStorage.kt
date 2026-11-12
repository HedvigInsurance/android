package com.hedvig.android.feature.home.home.data

import app.cash.turbine.Turbine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class FakeDismissedShopSessionsStorage(
  dismissedSessionIds: Set<String> = emptySet(),
) : DismissedShopSessionsStorage {
  val dismissedSessionIds = MutableStateFlow(dismissedSessionIds)
  val dismissedIdsTurbine = Turbine<String>()

  override fun observeDismissedSessionIds(): Flow<Set<String>> = dismissedSessionIds.asStateFlow()

  override suspend fun dismiss(sessionId: String) {
    dismissedSessionIds.update { it + sessionId }
    dismissedIdsTurbine.add(sessionId)
  }
}
