package com.hedvig.android.auth.event

import app.cash.turbine.Turbine

class FakeAuthEventListener : AuthEventListener {
  val loggedInEvent = Turbine<Unit>()
  val loggedOutEvent = Turbine<Unit>()

  override suspend fun loggedIn(accessToken: String) {
    // todo use in turbine
    loggedInEvent.add(Unit)
  }

  override suspend fun loggedOut() {
    loggedOutEvent.add(Unit)
  }
}
