package com.hedvig.android.auth.event

import com.hedvig.android.core.common.ApplicationScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import slimber.log.v
import kotlin.coroutines.CoroutineContext

class AuthEventBroadcaster(
  authEventListeners: Set<AuthEventListener>,
  applicationScope: ApplicationScope,
  coroutineContext: CoroutineContext,
) {
  private val authEvents = Channel<AuthEvent>(Channel.UNLIMITED)

  init {
    applicationScope.launch(coroutineContext) {
      v { "AuthenticationEventDispatcher starting collection" }
      authEvents.consumeAsFlow()
        .collect { event ->
          v { "AuthenticationEventDispatcher dispatching event:$event" }
          authEventListeners.map { listener ->
            async {
              when (event) {
                AuthEvent.LOGGED_IN -> listener.loggedIn()
                AuthEvent.LOGGED_OUT -> listener.loggedOut()
              }
            }
          }.awaitAll()
        }
    }
  }

  fun loggedIn() {
    authEvents.trySend(AuthEvent.LOGGED_IN)
  }

  fun loggedOut() {
    authEvents.trySend(AuthEvent.LOGGED_OUT)
  }

  private enum class AuthEvent { LOGGED_IN, LOGGED_OUT }
}
