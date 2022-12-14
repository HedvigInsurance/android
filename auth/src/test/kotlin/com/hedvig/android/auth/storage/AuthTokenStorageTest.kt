package com.hedvig.android.auth.storage

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.RefreshToken
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.time.Duration.Companion.minutes

internal class AuthTokenStorageTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  @Test
  fun `storing tokens in AuthTokenStorage persists their expiration time minus the buffer`() = runTest {
    val authTokenStorage = authTokenStorage()

    val expiryInSeconds = 5.minutes.inWholeSeconds.toInt()
    authTokenStorage.updateTokens(
      AccessToken("", expiryInSeconds),
      RefreshToken("", expiryInSeconds),
    )

    val (accessToken, refreshToken) = authTokenStorage.getTokens().filterNotNull().first()

    val expectedTokenExpirationInSeconds =
      expiryInSeconds - AuthTokenStorage.expirationTimeBuffer.inWholeSeconds.toInt()
    assertThat(accessToken.expiryInSeconds).isEqualTo(expectedTokenExpirationInSeconds)
    assertThat(refreshToken.expiryInSeconds).isEqualTo(expectedTokenExpirationInSeconds)
  }

  private fun TestScope.authTokenStorage(): AuthTokenStorage {
    return AuthTokenStorage(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = backgroundScope,
      ),
    )
  }
}
