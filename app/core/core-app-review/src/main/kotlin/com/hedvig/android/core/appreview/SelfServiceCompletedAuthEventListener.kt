package com.hedvig.android.core.appreview

import com.hedvig.android.auth.event.AuthEventListener

internal class SelfServiceCompletedAuthEventListener(
  private val selfServiceCompletedEventManager: SelfServiceCompletedEventManager,
) : AuthEventListener {
  override suspend fun loggedOut() {
    selfServiceCompletedEventManager.resetSelfServiceCompletions()
  }
}
