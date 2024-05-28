package com.hedvig.android.auth.event

import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.initializable.Initializable
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

internal class AuthEventBroadcaster(
  private val authEventStorage: AuthEventStorage,
  private val authEventListeners: Set<AuthEventListener>,
  private val applicationScope: ApplicationScope,
  private val coroutineContext: CoroutineContext,
) : Initializable {
  override fun initialize() {
    applicationScope.launch(coroutineContext) {
      authEventStorage
        .authEvents
        .consumeAsFlow()
        .collect { event ->
          logcat(LogPriority.VERBOSE) {
            val listenersList = authEventListeners.joinToString { it::class.simpleName ?: it::class.toString() }
            "AuthenticationEventDispatcher dispatching event:$event to listeners:{$listenersList}"
          }
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
}
