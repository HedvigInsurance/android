package com.hedvig.android.auth.event

import kotlinx.coroutines.channels.Channel

class AuthEventStorage() {
  val authEvents = Channel<AuthEvent>(Channel.UNLIMITED)

  fun loggedIn(accessToken: String, refreshToken: String) {
    authEvents.trySend(AuthEvent.LoggedIn(accessToken, refreshToken))
  }

  fun loggedOut() {
    authEvents.trySend(AuthEvent.LoggedOut)
  }
}
