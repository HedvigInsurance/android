package com.hedvig.android.auth.storage

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.test.clock.TestClock
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

internal class AuthTokenStorageTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `storing tokens in AuthTokenStorage persists their expiration time`() = runTest {
    val clock = TestClock()
    val authTokenStorage = authTokenStorage(clock)
    val now = clock.now()

    val expiryInSeconds = 5.minutes.inWholeSeconds
    authTokenStorage.updateTokens(
      AccessToken("", expiryInSeconds),
      RefreshToken("", expiryInSeconds),
    )

    val (accessToken, refreshToken) = authTokenStorage.getTokens().filterNotNull().first()

    val expectedTokenExpirationInSeconds = now + 5.minutes
    assertThat(accessToken.expiryDate).isEqualTo(expectedTokenExpirationInSeconds)
    assertThat(refreshToken.expiryDate).isEqualTo(expectedTokenExpirationInSeconds)
  }

  private fun TestScope.authTokenStorage(clock: Clock): AuthTokenStorage {
    return AuthTokenStorage(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = backgroundScope,
      ),
      clock,
    )
  }
}
