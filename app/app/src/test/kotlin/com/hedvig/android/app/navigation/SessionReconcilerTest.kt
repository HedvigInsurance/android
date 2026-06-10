package com.hedvig.android.app.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import assertk.assertThat
import assertk.assertions.containsExactly
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.auth.token.AuthTokens
import com.hedvig.android.auth.token.LocalAccessToken
import com.hedvig.android.auth.token.LocalRefreshToken
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.RefreshToken
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

internal class SessionReconcilerTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `reconcile flips a freshly seeded Login root to Home when logged in`() = runTest {
    val reconciler = sessionReconciler(authStatus = loggedIn())
    val controller = controllerWith(LoginKey)

    reconciler.reconcile(controller)

    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `reconcile leaves a logged-out seed on the Login root`() = runTest {
    val reconciler = sessionReconciler(authStatus = AuthStatus.LoggedOut)
    val controller = controllerWith(LoginKey)

    reconciler.reconcile(controller)

    assertThat(controller.entries.toList()).containsExactly(LoginKey)
  }

  // The regression: a new Activity launched into an already-warm process (same singleton reconciler, no
  // saved state) seeds a fresh Login root. A once-per-process guard skipped it and stranded the member on
  // the marketing screen; reconcile must resolve every controller it is handed.
  @Test
  fun `reconcile re-resolves a fresh Login seed for a second controller in a warm process`() = runTest {
    val reconciler = sessionReconciler(authStatus = loggedIn())
    // First Activity resolves to Home and latches readiness.
    reconciler.reconcile(controllerWith(HomeKey))

    // Second Activity in the same process, seeded to Login because there was no saved state to restore.
    val secondController = controllerWith(LoginKey)
    reconciler.reconcile(secondController)

    assertThat(secondController.entries.toList()).containsExactly(HomeKey)
  }

  private fun controllerWith(vararg keys: HedvigNavKey) = BackstackController(
    mutableStateListOf(*keys),
    mutableStateMapOf(),
    mutableStateOf(null), // pendingDeepLink
    mutableStateOf(null), // stashedSession
  )

  private fun TestScope.sessionReconciler(authStatus: AuthStatus): SessionReconciler {
    val authTokenStorage = AuthTokenStorage(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder(),
        coroutineScope = backgroundScope,
      ),
    )
    return SessionReconciler(
      authTokenService = FakeAuthTokenService(authStatus),
      demoManager = FakeDemoManager(),
      memberIdService = MemberIdService(authTokenStorage),
    )
  }

  private fun loggedIn(): AuthStatus.LoggedIn {
    val expiry = Clock.System.now() + 1.hours
    return AuthStatus.LoggedIn(
      LocalAccessToken("access-token", expiry),
      LocalRefreshToken("refresh-token", expiry),
    )
  }
}

private class FakeAuthTokenService(authStatus: AuthStatus) : AuthTokenService {
  private val state = MutableStateFlow<AuthStatus?>(authStatus)
  override val authStatus: StateFlow<AuthStatus?> = state.asStateFlow()

  override suspend fun getTokens(): AuthTokens? = error("Not used in these tests")

  override suspend fun refreshAndGetAccessToken(): AccessToken? = error("Not used in these tests")

  override suspend fun loginWithTokens(accessToken: AccessToken, refreshToken: RefreshToken) =
    error("Not used in these tests")

  override suspend fun logoutAndInvalidateTokens() = error("Not used in these tests")
}

private class FakeDemoManager : DemoManager {
  private val demoMode = MutableStateFlow(false)

  override fun isDemoMode(): Flow<Boolean> = demoMode.asStateFlow()

  override suspend fun setDemoMode(demoMode: Boolean) {
    this.demoMode.value = demoMode
  }
}
