package com.hedvig.android.auth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.auth.event.AuthEventStorage
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.auth.test.FakeAuthRepository
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.test.clock.TestClock
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.RefreshToken
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

internal class AuthTokenServiceImplTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `authStatus is LoggedOut when the stored refresh token is expired`() = runTest {
    val clock = TestClock()
    val storage = authTokenStorage(clock)
    storage.updateTokens(
      accessToken = AccessToken("access", expiryInSeconds = 60),
      refreshToken = RefreshToken("refresh", expiryInSeconds = 120),
    )
    clock.advanceTimeBy(121.seconds)
    val service = authTokenService(storage, clock)

    val status = service.authStatus.filterNotNull().first()

    assertThat(status).isEqualTo(AuthStatus.LoggedOut)
  }

  @Test
  fun `authStatus is LoggedIn when the stored refresh token is still valid`() = runTest {
    val clock = TestClock()
    val storage = authTokenStorage(clock)
    storage.updateTokens(
      accessToken = AccessToken("access", expiryInSeconds = 60),
      refreshToken = RefreshToken("refresh", expiryInSeconds = 120),
    )
    clock.advanceTimeBy(30.seconds)
    val service = authTokenService(storage, clock)

    val status = service.authStatus.filterNotNull().first()

    assertThat(status).isInstanceOf(AuthStatus.LoggedIn::class)
  }

  private fun TestScope.authTokenService(storage: AuthTokenStorage, clock: Clock): AuthTokenService =
    AuthTokenServiceImpl(
      storage,
      FakeAuthRepository(),
      AuthEventStorage(),
      ApplicationScope(backgroundScope),
      clock,
    )

  private fun TestScope.authTokenStorage(clock: Clock) = AuthTokenStorage(
    TestPreferencesDataStore(
      datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
      coroutineScope = backgroundScope,
    ),
    clock,
  )
}
