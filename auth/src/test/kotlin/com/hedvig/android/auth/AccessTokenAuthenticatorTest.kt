package com.hedvig.android.auth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.hedvig.android.auth.interceptor.AccessTokenAuthenticator
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.RefreshToken
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Authenticator
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AccessTokenAuthenticatorTest {

  @get:Rule
  val testFolder = TemporaryFolder()

  private val expiredToken = "expiredToken"

  private val testAuthRepository = FakeAuthRepository()

  @Test
  fun shouldNotAddAuthHeaderWhenResponseIs200() = runTest {
    val authTokenService = testAuthTokenService(testAuthRepository)
    val authenticator = testAccessTokenAuthenticator(authTokenService)
    val okHttpClient = okHttpClient(authenticator)

    val mockWebServer = MockWebServer()
    val successResponse = MockResponse().setBody("")
    mockWebServer.enqueue(successResponse)
    mockWebServer.start()

    val url = mockWebServer.url("")
    val request = buildRequest(url)
    okHttpClient.newCall(request).execute()

    val firstRequest = mockWebServer.takeRequest()
    assertThat(firstRequest.headers["Authorization"]).isEqualTo(expiredToken)
  }

  @Test
  fun shouldAddAuthHeaderWhenResponseIs401() = runTest(UnconfinedTestDispatcher()) {
    val authTokenService = testAuthTokenService(testAuthRepository)
    val authenticator = testAccessTokenAuthenticator(authTokenService)
    val okHttpClient = okHttpClient(authenticator)

    val mockWebServer = MockWebServer()
    val errorResponse = MockResponse().setResponseCode(401).setBody("")
    val successResponse = MockResponse().setBody("")
    mockWebServer.enqueue(errorResponse)
    mockWebServer.enqueue(successResponse)
    mockWebServer.start()
    testAuthRepository.exchangeResponse.add(AuthTokenResult.Success(refreshedAccessToken, refreshedRefreshToken))

    val url = mockWebServer.url("")
    val request = buildRequest(url)
    okHttpClient.newCall(request).execute()

    // First request with expired token
    val firstRequest = mockWebServer.takeRequest()
    assertThat(firstRequest.headers["Authorization"]).isEqualTo(expiredToken)
    // Second request intercepted by authenticator, adding an updated token
    val secondRequest = mockWebServer.takeRequest()
    assertThat(secondRequest.headers["Authorization"]).isEqualTo(refreshedAccessToken.token)
  }

  @Test
  fun shouldPersistNewTokenWhenResponseIs401AndCallIsSuccessful() = runTest(UnconfinedTestDispatcher()) {
    val authTokenService = testAuthTokenService(testAuthRepository)
    val authenticator = testAccessTokenAuthenticator(authTokenService)
    val okHttpClient = okHttpClient(authenticator)

    val mockWebServer = MockWebServer()
    val errorResponse = MockResponse().setResponseCode(401).setBody("")
    val successResponse = MockResponse().setBody("")
    mockWebServer.enqueue(errorResponse)
    mockWebServer.enqueue(successResponse)
    mockWebServer.start()

    testAuthRepository.exchangeResponse.add(AuthTokenResult.Success(refreshedAccessToken, refreshedRefreshToken))

    val url = mockWebServer.url("")
    val request = buildRequest(url)
    okHttpClient.newCall(request).execute()

    assertThat((authTokenService.authStatus.value as AuthStatus.LoggedIn).refreshToken.token)
      .isEqualTo(refreshedRefreshToken.token)
    assertThat(authTokenService.getToken()?.token).isEqualTo(refreshedAccessToken.token)
  }

  @Test
  fun shouldNotPersistNewTokenWhenResponseIs200() = runTest(UnconfinedTestDispatcher()) {
    val authTokenService = testAuthTokenService(testAuthRepository)
    val authenticator = testAccessTokenAuthenticator(authTokenService)
    val okHttpClient = okHttpClient(authenticator)

    val mockWebServer = MockWebServer()
    val successResponse = MockResponse().setBody("")
    mockWebServer.enqueue(successResponse)
    mockWebServer.start()

    val url = mockWebServer.url("")
    val request = buildRequest(url)
    okHttpClient.newCall(request).execute()

    assertThat((authTokenService.authStatus.value as AuthStatus.LoggedIn).refreshToken.token)
      .isEqualTo("oldRefreshToken")
    assertThat(authTokenService.getToken()?.token).isEqualTo(originalAccessToken.token)
  }

  @Test
  fun shouldRemovePersistedTokensWhenAuthCallResponseFails() = runTest(UnconfinedTestDispatcher()) {
    val authTokenService = testAuthTokenService(testAuthRepository)
    val authenticator = testAccessTokenAuthenticator(authTokenService)
    val okHttpClient = okHttpClient(authenticator)

    val mockWebServer = MockWebServer()
    val errorResponse = MockResponse().setResponseCode(401).setBody("")
    mockWebServer.enqueue(errorResponse)
    mockWebServer.start()

    assertThat((authTokenService.authStatus.value as AuthStatus.LoggedIn).refreshToken.token)
      .isEqualTo(originalRefreshToken.token)
    assertThat(authTokenService.getToken()?.token).isEqualTo(originalAccessToken.token)

    testAuthRepository.exchangeResponse.add(AuthTokenResult.Error("Error"))

    val url = mockWebServer.url("")
    val request = buildRequest(url)
    okHttpClient.newCall(request).execute()

    assertThat(authTokenService.authStatus.value)
      .isNotNull()
      .isInstanceOf(AuthStatus.LoggedOut::class)
    assertThat(authTokenService.getToken()).isEqualTo(null)
  }

  private fun okHttpClient(authenticator: Authenticator) = OkHttpClient
    .Builder()
    .authenticator(authenticator)
    .build()

  private fun testAccessTokenAuthenticator(authTokenService: AuthTokenService): Authenticator {
    return AccessTokenAuthenticator(authTokenService)
  }

  private fun TestScope.testAuthTokenService(
    authRepository: AuthRepository,
  ): AuthTokenService {
    val authTokenService = AuthTokenServiceImpl(
      authTokenStorage = AuthTokenStorage(
        dataStore = TestPreferencesDataStore(
          datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
          coroutineScope = backgroundScope,
        ),
      ),
      authRepository = authRepository,
      coroutineScope = backgroundScope,
    )
    authTokenService.updateTokens(originalAccessToken, originalRefreshToken)
    return authTokenService
  }

  private fun buildRequest(url: HttpUrl) = Request
    .Builder()
    .header("Authorization", expiredToken)
    .url(url)
    .build()

  companion object {
    private val originalAccessToken = AccessToken("oldToken", 100)
    private val originalRefreshToken = RefreshToken("oldRefreshToken", 100)

    private val refreshedAccessToken = AccessToken("newToken", 1000)
    private val refreshedRefreshToken = RefreshToken("newRefreshToken", 1000)
  }
}
