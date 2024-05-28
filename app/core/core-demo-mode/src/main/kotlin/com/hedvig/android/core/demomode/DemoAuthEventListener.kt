package com.hedvig.android.core.demomode

import com.hedvig.android.auth.event.AuthEventListener

internal class DemoAuthEventListener(
  private val demoManager: DemoManager,
) : AuthEventListener {
  override suspend fun loggedOut() {
    demoManager.setDemoMode(false)
  }
}
