package com.hedvig.android.app.navigation

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import arrow.core.identity
import arrow.fx.coroutines.raceN
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
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
 *  1. [reconcile] picks the start scene (Home when logged in / in demo, Login otherwise) before the
 *     splash is dismissed, so the first frame is correct rather than the seeded Login root. This
 *     reconciler is 1:1 with its Activity's [BackstackController] (both [ActivityRetainedScope]-scoped),
 *     so a warm-process relaunch builds a fresh Activity and so a fresh controller + reconciler, which
 *     can't strand a logged-in member on the marketing screen. [isReady] gates the Activity's splash
 *     keep-condition: it starts false, latches true once the root matches the auth state, and then
 *     stays true for this reconciler's life — so a config change (which reuses the retained reconciler)
 *     resolves nothing and never dips [isReady] back to false.
 *  2. [observeForcedLogout] keeps the root honest while running: log out when we leave demo mode and no
 *     longer hold tokens. It is lifecycle-gated so it only observes while the UI is STARTED.
 *
 * Deliberately narrow — only auth and the backstack root. Deep-links, notifications and the rest stay
 * in their own observers. Scoped to the per-Activity [ActivityRetainedScope] so it shares this
 * Activity's [BackstackController]; only the [Lifecycle] is handed in per call by the composition,
 * keeping this reconciler free of any Android/Compose lifetime.
 */
@SingleIn(ActivityRetainedScope::class)
@Inject
internal class SessionReconciler(
  private val backstackController: BackstackController,
  private val authTokenService: AuthTokenService,
  private val demoManager: DemoManager,
  private val memberIdService: MemberIdService,
) {
  private val isReadyState = MutableStateFlow(false)

  /** False until the start scene has been resolved; latches true once the first splash is dismissed. */
  val isReady: StateFlow<Boolean> = isReadyState.asStateFlow()

  private var lastKnownMemberId: String? = null

  /**
   * Resolves the start scene for this Activity's [backstackController] before the splash is dismissed,
   * then latches [isReady]. Runs its body at most once per instance — the early return makes a second
   * call (e.g. after a config change reuses the retained instance) a no-op, so [isReady] never dips back
   * to false and the content gate keyed on it never blanks.
   *
   * The guard is correct because this reconciler is [ActivityRetainedScope]-scoped, 1:1 with its
   * Activity. A config change reuses the retained reconciler, whose [backstackController] root already
   * matches the auth state, so re-resolving would be redundant. Anything that seeds a fresh `LoginKey`
   * root — a cold start or a warm-process relaunch — builds a new Activity and therefore a new
   * reconciler whose [isReady] starts false, so the guard never skips a root that still needs resolving.
   */
  suspend fun reconcile() {
    if (isReadyState.value) return
    memberIdService.getMemberId().first()?.let { lastKnownMemberId = it }
    determineStartScene()
    isReadyState.value = true
  }

  /**
   * Lifecycle-gated observers that keep the rendered root honest while the UI is STARTED: tracks the
   * latest member id and logs out when we leave demo mode without holding tokens.
   */
  suspend fun observeForcedLogout(lifecycle: Lifecycle) {
    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
      launch {
        memberIdService.getMemberId().collect { id ->
          if (id != null) lastKnownMemberId = id
        }
      }
      logoutOnInvalidCredentials()
    }
  }

  /**
   * Holds until the auth state resolves, then makes the back stack root match it. On process-death
   * restore the back stack already reflects the previous session, so a matching root is left untouched
   * and any deeper stack is preserved.
   */
  private suspend fun determineStartScene() {
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
  private suspend fun logoutOnInvalidCredentials() {
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
