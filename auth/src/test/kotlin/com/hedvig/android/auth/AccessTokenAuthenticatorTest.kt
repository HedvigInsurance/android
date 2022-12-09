package com.hedvig.android.auth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.hedvig.android.auth.interceptor.AccessTokenAuthenticator
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.Grant
import com.hedvig.authlib.LoginMethod
import com.hedvig.authlib.LoginStatusResult
import com.hedvig.authlib.RefreshToken
import com.hedvig.authlib.ResendOtpResult
import com.hedvig.authlib.RevokeResult
import com.hedvig.authlib.StatusUrl
import com.hedvig.authlib.SubmitOtpResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
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

  private var submitAuthResult: AuthTokenResult = AuthTokenResult.Success(
    accessToken = AccessToken(
      token = "newToken",
      expiryInSeconds = 1000,
    ),
    refreshToken = RefreshToken(
      token = "newRefreshToken",
      expiryInSeconds = 1000,
    ),
  )

  private val testAuthRepository = object : AuthRepository {
    override suspend fun startLoginAttempt(
      loginMethod: LoginMethod,
      market: String,
      personalNumber: String?,
      email: String?,
    ): AuthAttemptResult {
      return AuthAttemptResult.Error("Should not use in this test")
    }

    override fun observeLoginStatus(statusUrl: StatusUrl): Flow<LoginStatusResult> {
      return flowOf()
    }

    override suspend fun submitOtp(verifyUrl: String, otp: String): SubmitOtpResult {
      return SubmitOtpResult.Error("Should not use in this test")
    }

    override suspend fun resendOtp(resendUrl: String): ResendOtpResult {
      return ResendOtpResult.Success
    }

    override suspend fun exchange(grant: Grant): AuthTokenResult {
      return submitAuthResult
    }

    override suspend fun loginStatus(statusUrl: StatusUrl): LoginStatusResult {
      return LoginStatusResult.Failed("Should not use in this test")
    }

    override suspend fun revoke(token: String): RevokeResult {
      return RevokeResult.Error("Should not use in this test")
    }
  }

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

    val url = mockWebServer.url("")
    val request = buildRequest(url)
    okHttpClient.newCall(request).execute()

    // First request with expired token
    val firstRequest = mockWebServer.takeRequest()
    assertThat(firstRequest.headers["Authorization"]).isEqualTo(expiredToken)
    // Second request intercepted by authenticator, adding an updated token
    val secondRequest = mockWebServer.takeRequest()
    assertThat(secondRequest.headers["Authorization"]).isEqualTo("newToken")
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

    val url = mockWebServer.url("")
    val request = buildRequest(url)
    okHttpClient.newCall(request).execute()

    assertThat((authTokenService.authStatus().value as AuthStatus.LoggedIn).refreshToken.token)
      .isEqualTo("newRefreshToken")
    assertThat(authTokenService.getToken()?.token).isEqualTo("newToken")
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

    assertThat((authTokenService.authStatus().value as AuthStatus.LoggedIn).refreshToken.token)
      .isEqualTo("oldRefreshToken")
    assertThat(authTokenService.getToken()?.token).isEqualTo("oldToken")
  }

  @Test
  fun shouldRemovePersistedTokensWhenAuthCallResponseFails() = runTest(UnconfinedTestDispatcher()) {
    val authTokenService = testAuthTokenService(testAuthRepository)
    val authenticator = testAccessTokenAuthenticator(authTokenService)
    val okHttpClient = okHttpClient(authenticator)
    submitAuthResult = AuthTokenResult.Error("Error")

    val mockWebServer = MockWebServer()
    val errorResponse = MockResponse().setResponseCode(401).setBody("")
    mockWebServer.enqueue(errorResponse)
    mockWebServer.start()

    assertThat((authTokenService.authStatus().value as AuthStatus.LoggedIn).refreshToken.token)
      .isEqualTo("oldRefreshToken")
    assertThat(authTokenService.getToken()?.token).isEqualTo("oldToken")

    val url = mockWebServer.url("")
    val request = buildRequest(url)
    okHttpClient.newCall(request).execute()

    assertThat(authTokenService.authStatus().value)
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
          backgroundScope,
        ),
      ),
      authRepository = authRepository,
      coroutineScope = backgroundScope,
    )
    authTokenService.updateTokens(
      AccessToken("oldToken", 100),
      RefreshToken("oldRefreshToken", 100),
    )
    runCurrent()
    return authTokenService
  }

  private fun buildRequest(url: HttpUrl) = Request
    .Builder()
    .header("Authorization", expiredToken)
    .url(url)
    .build()
}
