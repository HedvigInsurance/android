package com.hedvig.android.auth.interceptor

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.auth.AndroidAccessTokenProvider
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.AuthTokenServiceImpl
import com.hedvig.android.auth.event.AuthEventStorage
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.auth.test.FakeAuthRepository
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.test.clock.TestClock
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.RefreshToken
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AndroidAccessTokenProviderTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `when providing a simple token, returns the token`() = runTest {
    val clock = TestClock()
    val authTokenStorage = authTokenStorage(clock)
    authTokenStorage.updateTokens(
      AccessToken("token", 10.minutes.inWholeSeconds),
      RefreshToken("", 0),
    )
    val authTokenService = authTokenService(authTokenStorage)
    val accessTokenProvider = AndroidAccessTokenProvider(authTokenService, clock)

    val token = accessTokenProvider.provide()

    assertThat(token).isEqualTo("token")
  }

  @Test
  fun `when a token already exists, simply returns the token`() = runTest {
    val clock = TestClock()
    val authTokenStorage = authTokenStorage(clock)
    authTokenStorage.updateTokens(
      AccessToken("token", 10.minutes.inWholeSeconds),
      RefreshToken("", 0),
    )
    val authTokenService = authTokenService(authTokenStorage)
    val accessTokenProvider = AndroidAccessTokenProvider(authTokenService, clock)

    val token = accessTokenProvider.provide()

    assertThat(token).isEqualTo("token")
  }

  @Test
  fun `when the access token is expired, and the refresh token is not expired, refresh and return new token`() = runTest {
    val clock = TestClock()
    val authTokenStorage = authTokenStorage(clock)
    authTokenStorage.updateTokens(
      AccessToken("", 10.minutes.inWholeSeconds),
      RefreshToken("", 1.hours.inWholeSeconds),
    )
    val authRepository = FakeAuthRepository()
    val authTokenService = authTokenService(authTokenStorage, authRepository)
    val accessTokenProvider = AndroidAccessTokenProvider(authTokenService, clock)

    clock.advanceTimeBy(30.minutes)
    authRepository.exchangeResponse.add(
      AuthTokenResult.Success(
        AccessToken("refreshedToken", 10.minutes.inWholeSeconds),
        RefreshToken("refreshedRefreshToken", 0),
      ),
    )
    val token = accessTokenProvider.provide()

    val storedAuthTokens = authTokenStorage.getTokens().first()!!
    assertThat(storedAuthTokens.accessToken.token).isEqualTo("refreshedToken")
    assertThat(storedAuthTokens.refreshToken.token).isEqualTo("refreshedRefreshToken")
    assertThat(token).isEqualTo("refreshedToken")
  }

  @Test
  fun `when the access token and the refresh token are expired, clear tokens and return null`() = runTest {
    val clock = TestClock()
    val authTokenStorage = authTokenStorage(clock)
    authTokenStorage.updateTokens(
      AccessToken("", 10.minutes.inWholeSeconds),
      RefreshToken("", 1.hours.inWholeSeconds),
    )
    val authRepository = FakeAuthRepository()
    val authTokenService = authTokenService(authTokenStorage, authRepository)
    val accessTokenProvider = AndroidAccessTokenProvider(authTokenService, clock)

    clock.advanceTimeBy(1.hours)
    val token = accessTokenProvider.provide()

    val storedAuthTokens = authTokenStorage.getTokens().first()
    assertThat(storedAuthTokens).isNull()
    assertThat(token).isNull()
  }

  @Test
  fun `with two requests happening in parallel, the token is only refreshed once`() = runTest {
    val clock = TestClock()
    val authTokenStorage = authTokenStorage(clock)
    authTokenStorage.updateTokens(
      AccessToken("", 10.minutes.inWholeSeconds),
      RefreshToken("", 1.hours.inWholeSeconds),
    )
    val authRepository = FakeAuthRepository()
    val authTokenService = authTokenService(authTokenStorage, authRepository)
    val accessTokenProvider = AndroidAccessTokenProvider(authTokenService, clock)

    clock.advanceTimeBy(30.minutes)
    authRepository.exchangeResponse.add(
      AuthTokenResult.Success(
        AccessToken("refreshedToken", 10.minutes.inWholeSeconds),
        RefreshToken("refreshedRefreshToken", 0),
      ),
    )
    val token1Deferred = async { accessTokenProvider.provide() }
    val token2Deferred = async { accessTokenProvider.provide() }
    val (token1, token2) = awaitAll(token1Deferred, token2Deferred)

    val storedAuthTokens = authTokenStorage.getTokens().first()!!
    assertThat(storedAuthTokens.accessToken.token).isEqualTo("refreshedToken")
    assertThat(storedAuthTokens.refreshToken.token).isEqualTo("refreshedRefreshToken")
    assertThat(token1).isEqualTo("refreshedToken")
    assertThat(token2).isEqualTo("refreshedToken")
  }

  @Test
  fun `two requests happen in parallel, the token is only refreshed once, then a third one gets the token eagerly`() =
    runTest {
      val clock = TestClock()
      val authTokenStorage = authTokenStorage(clock)
      authTokenStorage.updateTokens(
        AccessToken("", 10.minutes.inWholeSeconds),
        RefreshToken("", 1.hours.inWholeSeconds),
      )
      val authRepository = FakeAuthRepository()
      val authTokenService = authTokenService(authTokenStorage, authRepository)
      val accessTokenProvider = AndroidAccessTokenProvider(authTokenService, clock)

      clock.advanceTimeBy(30.minutes)
      authRepository.exchangeResponse.add(
        AuthTokenResult.Success(
          AccessToken("refreshedToken", 10.minutes.inWholeSeconds),
          RefreshToken("refreshedRefreshToken", 0),
        ),
      )
      val token1Deferred = async { accessTokenProvider.provide() }
      val token2Deferred = async { accessTokenProvider.provide() }

      val (token1, token2) = awaitAll(token1Deferred, token2Deferred)

      val storedAuthTokens = authTokenStorage.getTokens().first()!!
      assertThat(storedAuthTokens.accessToken.token).isEqualTo("refreshedToken")
      assertThat(storedAuthTokens.refreshToken.token).isEqualTo("refreshedRefreshToken")

      assertThat(token1).isEqualTo("refreshedToken")
      assertThat(token2).isEqualTo("refreshedToken")

      val token3 = accessTokenProvider.provide()
      assertThat(token3).isEqualTo("refreshedToken")
    }

  private fun TestScope.authTokenService(
    authTokenStorage: AuthTokenStorage,
    fakeAuthRepository: FakeAuthRepository = FakeAuthRepository(),
  ): AuthTokenService {
    return AuthTokenServiceImpl(
      authTokenStorage,
      fakeAuthRepository,
      AuthEventStorage(),
      backgroundScope,
    )
  }

  private fun TestScope.authTokenStorage(clock: Clock) = AuthTokenStorage(
    TestPreferencesDataStore(
      datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
      coroutineScope = backgroundScope,
    ),
    clock,
  )
}
