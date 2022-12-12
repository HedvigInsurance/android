package com.hedvig.android.auth

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.auth.network.AccessTokenAuthenticator
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
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test

class AccessTokenAuthenticatorTest {

  private val expiredToken = "expiredToken"

  private val mockAuthenticationTokenService = object : AuthenticationTokenService {
    override var authenticationToken: String? = "oldToken"
    override var refreshToken: RefreshToken? = RefreshToken(
      token = "oldRefreshToken",
      expiryInSeconds = 100,
    )
  }

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

  private val authRepository = object : AuthRepository {
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

  private val authenticator = AccessTokenAuthenticator(
    authenticationTokenService = mockAuthenticationTokenService,
    authRepository = authRepository,
  )

  private val okHttpClient = OkHttpClient
    .Builder()
    .authenticator(authenticator)
    .build()

  @Test
  fun shouldNotAddAuthHeaderWhenResponseIs200() {
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
  fun shouldAddAuthHeaderWhenResponseIs401() {
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
  fun shouldPersistNewTokenWhenResponseIs401AndCallIsSuccessFull() {
    val mockWebServer = MockWebServer()
    val errorResponse = MockResponse().setResponseCode(401).setBody("")
    val successResponse = MockResponse().setBody("")
    mockWebServer.enqueue(errorResponse)
    mockWebServer.enqueue(successResponse)
    mockWebServer.start()

    val url = mockWebServer.url("")
    val request = buildRequest(url)
    okHttpClient.newCall(request).execute()

    assertThat(mockAuthenticationTokenService.refreshToken?.token).isEqualTo("newRefreshToken")
    assertThat(mockAuthenticationTokenService.authenticationToken).isEqualTo("newToken")
  }

  @Test
  fun shouldNotPersistNewTokenWhenResponseIs200() {
    val mockWebServer = MockWebServer()
    val successResponse = MockResponse().setBody("")
    mockWebServer.enqueue(successResponse)
    mockWebServer.start()

    val url = mockWebServer.url("")
    val request = buildRequest(url)
    okHttpClient.newCall(request).execute()

    assertThat(mockAuthenticationTokenService.refreshToken?.token).isEqualTo("oldRefreshToken")
    assertThat(mockAuthenticationTokenService.authenticationToken).isEqualTo("oldToken")
  }

  @Test
  fun shouldRemovePersistedTokensWhenAuthCallResponseFails() {
    submitAuthResult = AuthTokenResult.Error("Error")

    val mockWebServer = MockWebServer()
    val errorResponse = MockResponse().setResponseCode(401).setBody("")
    mockWebServer.enqueue(errorResponse)
    mockWebServer.start()

    assertThat(mockAuthenticationTokenService.refreshToken?.token).isEqualTo("oldRefreshToken")
    assertThat(mockAuthenticationTokenService.authenticationToken).isEqualTo("oldToken")

    val url = mockWebServer.url("")
    val request = buildRequest(url)
    okHttpClient.newCall(request).execute()

    assertThat(mockAuthenticationTokenService.refreshToken?.token).isEqualTo(null)
    assertThat(mockAuthenticationTokenService.authenticationToken).isEqualTo(null)
  }

  private fun buildRequest(url: HttpUrl) = Request
    .Builder()
    .header("Authorization", expiredToken)
    .url(url)
    .build()
}
