package com.hedvig.android.auth.event

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.channels.Channel

@SingleIn(AppScope::class)
@Inject
class AuthEventStorage() {
  val authEvents = Channel<AuthEvent>(Channel.UNLIMITED)

  fun loggedIn(accessToken: String, refreshToken: String) {
    authEvents.trySend(AuthEvent.LoggedIn(accessToken, refreshToken))
  }

  fun loggedOut() {
    authEvents.trySend(AuthEvent.LoggedOut)
  }
}
