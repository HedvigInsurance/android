package com.hedvig.android.app.navigation

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import arrow.fx.coroutines.raceN
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Owns the auth↔backstack reconciliation that used to live as loose effects in `HedvigApp`. Two jobs,
 * both narrowly about the rendered root:
 *  1. Pick the start scene (Home when logged in / in demo, Login otherwise) before the splash is
 *     dismissed, so the first frame is correct rather than the seeded Login root. [isReady] flips true
 *     once that resolves and drives the Activity's splash keep-condition.
 *  2. Keep the root honest while running: log out when we leave demo mode and no longer hold tokens.
 *
 * Deliberately narrow — only auth and the backstack root. Deep-links, notifications and the rest stay
 * in their own observers. [BackstackController] is composition + saved-state scoped, so it can't be a
 * constructor dependency; it (and the [Lifecycle]) are handed to [reconcile] by the composition.
 */
@SingleIn(AppScope::class)
@Inject
internal class SessionReconciler(
  private val authTokenService: AuthTokenService,
  private val demoManager: DemoManager,
  private val memberIdService: MemberIdService,
) {
  private val isReadyState = MutableStateFlow(false)

  /** False until the start scene has been resolved; latches true for the process lifetime. */
  val isReady: StateFlow<Boolean> = isReadyState.asStateFlow()

  private var lastKnownMemberId: String? = null

  suspend fun reconcile(backstackController: BackstackController, lifecycle: Lifecycle): Unit = coroutineScope {
    launch {
      memberIdService.getMemberId().collect { id ->
        if (id != null) lastKnownMemberId = id
      }
    }
    if (!isReadyState.value) {
      determineStartScene(backstackController)
      isReadyState.value = true
    }
    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
      logoutOnInvalidCredentials(backstackController)
    }
  }

  /**
   * Holds until the auth state resolves, then makes the back stack root match it. On process-death
   * restore the back stack already reflects the previous session, so a matching root is left untouched
   * and any deeper stack is preserved.
   */
  private suspend fun determineStartScene(backstackController: BackstackController) {
    val showLoggedInScene = raceN(
      { authTokenService.authStatus.filterNotNull().first() is AuthStatus.LoggedIn },
      { demoManager.isDemoMode().first { it } },
    ).fold({ it }, { it })
    when {
      showLoggedInScene && !backstackController.isLoggedIn -> {
        // Token is present now (authStatus is LoggedIn); read the member id straight from the JWT.
        val memberId = memberIdService.getMemberId().first()
        backstackController.setLoggedIn(memberId)
      }

      !showLoggedInScene && backstackController.isLoggedIn -> {
        backstackController.setLoggedOut(lastKnownMemberId)
      }
    }
  }

  /**
   * Automatically logs out when we are no longer in demo mode and we are also not considered to have
   * active tokens.
   */
  private suspend fun logoutOnInvalidCredentials(backstackController: BackstackController) {
    val authStatusLog: (AuthStatus?) -> Unit = { authStatus ->
      logcat {
        buildString {
          append("Owner: MainActivity | Received authStatus: ")
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
    combine(
      authTokenService.authStatus.onEach(authStatusLog).filterNotNull().distinctUntilChanged(),
      demoManager.isDemoMode().distinctUntilChanged(),
      snapshotFlow { backstackController.isLoggedIn },
    ) { authStatus: AuthStatus, isDemoMode: Boolean, isLoggedIn: Boolean ->
      logcat {
        "SessionReconciler.logoutOnInvalidCredentials: " +
          "authStatus:$authStatus | " +
          "isDemoMode:$isDemoMode | " +
          "isLoggedIn:$isLoggedIn"
      }
      if (!isLoggedIn) {
        return@combine
      }
      if (!isDemoMode && authStatus !is AuthStatus.LoggedIn) {
        backstackController.setLoggedOut(lastKnownMemberId)
      }
    }.collect()
  }
}
