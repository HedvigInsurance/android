package com.hedvig.android.feature.home.home.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class FakeDismissedShopSessionsStorage : DismissedShopSessionsStorage {
  private val state = MutableStateFlow<Set<String>>(emptySet())

  override fun observeDismissedSessionIds(): Flow<Set<String>> = state.asStateFlow()

  override suspend fun clear() {
    state.update { emptySet() }
  }

  override suspend fun dismiss(sessionId: String) {
    state.update { it + sessionId }
  }
}
