package com.hedvig.android.auth.android

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenServiceProvider
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.activity.ActivityNavigator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class AuthenticatedObserver : DefaultLifecycleObserver {

  private val authTokenServiceProvider: AuthTokenServiceProvider by inject(AuthTokenServiceProvider::class.java)
  private val activityNavigator: ActivityNavigator by inject(ActivityNavigator::class.java)

  private var authObservingJob: Job? = null

  override fun onResume(owner: LifecycleOwner) {
    authObservingJob = owner.lifecycleScope.launch {
      authTokenServiceProvider.provide().authStatus
        .onEach { authStatus ->
          logcat {
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
      activityNavigator.navigateToMarketingActivity()
    }
  }

  override fun onPause(owner: LifecycleOwner) {
    authObservingJob?.cancel()
    authObservingJob = null
  }
}
