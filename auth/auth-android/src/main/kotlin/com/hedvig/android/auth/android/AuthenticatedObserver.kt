package com.hedvig.android.auth.android

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.navigation.Navigator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import slimber.log.d

class AuthenticatedObserver : DefaultLifecycleObserver {

  private val authTokenService: AuthTokenService by inject(AuthTokenService::class.java)
  private val navigator: Navigator by inject(Navigator::class.java)

  private var authObservingJob: Job? = null

  override fun onResume(owner: LifecycleOwner) {
    authObservingJob = owner.lifecycleScope.launch {
      authTokenService.authStatus
        .onEach { authStatus ->
          d {
            buildString {
              append("Owner: ${owner::class.simpleName} | Received authStatus: ")
              append(
                when (authStatus) {
                  is AuthStatus.LoggedIn -> "LoggedIn"
                  AuthStatus.LoggedOut -> "LoggedOut"
                  null -> "null"
                },
              )
            }
          }
        }
        .filterIsInstance<AuthStatus.LoggedOut>()
        .first()
      navigator.navigateToMarketingActivity()
    }
  }

  override fun onPause(owner: LifecycleOwner) {
    authObservingJob?.cancel()
    authObservingJob = null
  }
}
