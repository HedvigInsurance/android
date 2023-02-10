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
                is AuthEvent.LoggedIn -> listener.loggedIn(event.accessToken)
                AuthEvent.LoggedOut -> listener.loggedOut()
              }
            }
          }.awaitAll()
        }
    }
  }

  fun loggedIn(accessToken: String) {
    authEvents.trySend(AuthEvent.LoggedIn(accessToken))
  }

  fun loggedOut() {
    authEvents.trySend(AuthEvent.LoggedOut)
  }

  private sealed interface AuthEvent {
    data class LoggedIn(val accessToken: String) : AuthEvent
    object LoggedOut : AuthEvent
  }
}
