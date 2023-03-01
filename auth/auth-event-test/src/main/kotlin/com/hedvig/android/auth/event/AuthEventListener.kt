package com.hedvig.android.auth.event

import app.cash.turbine.Turbine

class FakeAuthEventListener : AuthEventListener {
  val loggedInEvent = Turbine<String>()
  val loggedOutEvent = Turbine<Unit>()

  override suspend fun loggedIn(accessToken: String) {
    loggedInEvent.add(accessToken)
  }

  override suspend fun loggedOut() {
    loggedOutEvent.add(Unit)
  }
}
