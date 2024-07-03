package com.hedvig.android.auth.interceptor

import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

// TODO("todo does not work without authlib")
// class AuthTokenRefreshingInterceptorTest {
//  @get:Rule
//  val testFolder = TemporaryFolder()
//
//  @get:Rule
//  val testLogcatLogger = TestLogcatLoggingRule()
//
//  @Test
//  fun `The token in the header contains the 'Bearer ' prefix as the backend expects it`() = runTest {
//    val webServer = MockWebServer().also { it.enqueue(MockResponse()) }
//    val okHttpClient = testOkHttpClient(
//      AuthTokenRefreshingInterceptor(accessTokenProvider = { "token" }),
//    )
//
//    okHttpClient.newCall(webServer.testRequest()).enqueueAndSuspendUntilCallbackIgnoringResponse()
//    val requestSent: RecordedRequest = webServer.takeRequest()
//
//    assertThat(requestSent.headers["Authorization"]).isEqualTo("Bearer token")
//  }
//
//  @Test
//  fun `when a token already exists, simply goes through adding the header`() = runTest {
//    val clock = TestClock()
//    val authTokenStorage = authTokenStorage(clock)
//    authTokenStorage.updateTokens(
//      AccessToken("token", 10.minutes.inWholeSeconds),
//      RefreshToken("", 0),
//    )
//    val authTokenService = authTokenService(authTokenStorage)
//    val interceptor = AuthTokenRefreshingInterceptor(accessTokenProvider(authTokenService, clock))
//    val webServer = MockWebServer().also { it.enqueue(MockResponse()) }
//    val okHttpClient = testOkHttpClient(interceptor)
//    runCurrent()
//
//    okHttpClient.newCall(webServer.testRequest()).enqueueAndSuspendUntilCallbackIgnoringResponse()
//    val requestSent: RecordedRequest = webServer.takeRequest()
//
//    assertThat(requestSent.headers["Authorization"]).isEqualTo("Bearer token")
//  }
//
//  @Test
//  fun `when the access token is expired, and the refresh token is not expired, refresh and add header`() = runTest {
//    val clock = TestClock()
//    val authTokenStorage = authTokenStorage(clock)
//    authTokenStorage.updateTokens(
//      AccessToken("", 10.minutes.inWholeSeconds),
//      RefreshToken("", 1.hours.inWholeSeconds),
//    )
//    val authRepository = FakeAuthRepository()
//    val authTokenService = authTokenService(authTokenStorage, authRepository)
//    val interceptor = AuthTokenRefreshingInterceptor(accessTokenProvider(authTokenService, clock))
//    val webServer = MockWebServer().also { it.enqueue(MockResponse()) }
//    val okHttpClient = testOkHttpClient(interceptor)
//    runCurrent()
//
//    clock.advanceTimeBy(30.minutes)
//    authRepository.exchangeResponse.add(
//      AuthTokenResult.Success(
//        AccessToken("refreshedToken", 10.minutes.inWholeSeconds),
//        RefreshToken("refreshedRefreshToken", 0),
//      ),
//    )
//    okHttpClient.newCall(webServer.testRequest()).enqueueAndSuspendUntilCallbackIgnoringResponse()
//    val requestSent: RecordedRequest = webServer.takeRequest()
//
//    val storedAuthTokens = authTokenStorage.getTokens().first()!!
//    assertThat(storedAuthTokens.accessToken.token).isEqualTo("refreshedToken")
//    assertThat(storedAuthTokens.refreshToken.token).isEqualTo("refreshedRefreshToken")
//    assertThat(requestSent.headers["Authorization"]).isEqualTo("Bearer refreshedToken")
//  }
//
//  @Test
//  fun `when the access token and the refresh token are expired, clear tokens and proceed without a header`() = runTest {
//    val clock = TestClock()
//    val authTokenStorage = authTokenStorage(clock)
//    authTokenStorage.updateTokens(
//      AccessToken("", 10.minutes.inWholeSeconds),
//      RefreshToken("", 1.hours.inWholeSeconds),
//    )
//    val authRepository = FakeAuthRepository()
//    val authTokenService = authTokenService(authTokenStorage, authRepository)
//    val interceptor = AuthTokenRefreshingInterceptor(accessTokenProvider(authTokenService, clock))
//    val webServer = MockWebServer().also { it.enqueue(MockResponse()) }
//    val okHttpClient = testOkHttpClient(interceptor)
//    runCurrent()
//
//    clock.advanceTimeBy(1.hours)
//    okHttpClient.newCall(webServer.testRequest()).enqueueAndSuspendUntilCallbackIgnoringResponse()
//    val requestSent: RecordedRequest = webServer.takeRequest()
//
//    val storedAuthTokens = authTokenStorage.getTokens().first()
//    assertThat(storedAuthTokens).isNull()
//    assertThat(requestSent.headers["Authorization"]).isNull()
//  }
//
//  @Test
//  fun `with two requests happening in parallel, the token is only refreshed once`() = runTest {
//    val clock = TestClock()
//    val authTokenStorage = authTokenStorage(clock)
//    authTokenStorage.updateTokens(
//      AccessToken("", 10.minutes.inWholeSeconds),
//      RefreshToken("", 1.hours.inWholeSeconds),
//    )
//    val authRepository = FakeAuthRepository()
//    val authTokenService = authTokenService(authTokenStorage, authRepository)
//    val interceptor = AuthTokenRefreshingInterceptor(accessTokenProvider(authTokenService, clock))
//    val webServer = MockWebServer().also { mockWebServer ->
//      repeat(2) { mockWebServer.enqueue(MockResponse()) }
//    }
//    val okHttpClient = testOkHttpClient(interceptor)
//    runCurrent()
//
//    clock.advanceTimeBy(30.minutes)
//    authRepository.exchangeResponse.add(
//      AuthTokenResult.Success(
//        AccessToken("refreshedToken", 10.minutes.inWholeSeconds),
//        RefreshToken("refreshedRefreshToken", 0),
//      ),
//    )
//    okHttpClient.newCall(webServer.testRequest()).enqueueAndSuspendUntilCallbackIgnoringResponse()
//    okHttpClient.newCall(webServer.testRequest()).enqueueAndSuspendUntilCallbackIgnoringResponse()
//    runCurrent()
//    val requestSent1: RecordedRequest = webServer.takeRequest()
//    val requestSent2: RecordedRequest = webServer.takeRequest()
//
//    val storedAuthTokens = authTokenStorage.getTokens().first()!!
//    assertThat(storedAuthTokens.accessToken.token).isEqualTo("refreshedToken")
//    assertThat(storedAuthTokens.refreshToken.token).isEqualTo("refreshedRefreshToken")
//    assertThat(requestSent1.headers["Authorization"]).isEqualTo("Bearer refreshedToken")
//    assertThat(requestSent2.headers["Authorization"]).isEqualTo("Bearer refreshedToken")
//  }
//
//  @Test
//  fun `two requests happen in parallel, the token is only refreshed once, then a third one gets the token eagerly`() =
//    runTest {
//      val clock = TestClock()
//      val authTokenStorage = authTokenStorage(clock)
//      authTokenStorage.updateTokens(
//        AccessToken("", 10.minutes.inWholeSeconds),
//        RefreshToken("", 1.hours.inWholeSeconds),
//      )
//      val authRepository = FakeAuthRepository()
//      val authTokenService = authTokenService(authTokenStorage, authRepository)
//      val interceptor = AuthTokenRefreshingInterceptor(accessTokenProvider(authTokenService, clock))
//      val webServer = MockWebServer().also { mockWebServer ->
//        repeat(3) { mockWebServer.enqueue(MockResponse()) }
//      }
//      val okHttpClient = testOkHttpClient(interceptor)
//      runCurrent()
//
//      clock.advanceTimeBy(30.minutes)
//      authRepository.exchangeResponse.add(
//        AuthTokenResult.Success(
//          AccessToken("refreshedToken", 10.minutes.inWholeSeconds),
//          RefreshToken("refreshedRefreshToken", 0),
//        ),
//      )
//      okHttpClient.newCall(webServer.testRequest()).enqueueAndSuspendUntilCallbackIgnoringResponse()
//      okHttpClient.newCall(webServer.testRequest()).enqueueAndSuspendUntilCallbackIgnoringResponse()
//      runCurrent()
//
//      okHttpClient.newCall(webServer.testRequest()).enqueueAndSuspendUntilCallbackIgnoringResponse()
//
//      val requestSent1: RecordedRequest = webServer.takeRequest()
//      val requestSent2: RecordedRequest = webServer.takeRequest()
//      val requestSent3: RecordedRequest = webServer.takeRequest()
//
//      val storedAuthTokens = authTokenStorage.getTokens().first()!!
//      assertThat(storedAuthTokens.accessToken.token).isEqualTo("refreshedToken")
//      assertThat(storedAuthTokens.refreshToken.token).isEqualTo("refreshedRefreshToken")
//      assertThat(requestSent1.headers["Authorization"]).isEqualTo("Bearer refreshedToken")
//      assertThat(requestSent2.headers["Authorization"]).isEqualTo("Bearer refreshedToken")
//      assertThat(requestSent3.headers["Authorization"]).isEqualTo("Bearer refreshedToken")
//    }
//
//  private fun MockWebServer.testRequest(): Request {
//    return Request.Builder().url(url("test")).build()
//  }
//
//  private fun TestScope.authTokenService(
//    authTokenStorage: AuthTokenStorage,
//    fakeAuthRepository: FakeAuthRepository = FakeAuthRepository(),
//  ): AuthTokenService {
//    return AuthTokenServiceImpl(
//      authTokenStorage,
//      fakeAuthRepository,
//      AuthEventStorage(),
//      backgroundScope,
//    )
//  }
//
//  private fun TestScope.authTokenStorage(clock: Clock) = AuthTokenStorage(
//    TestPreferencesDataStore(
//      datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
//      coroutineScope = backgroundScope,
//    ),
//    clock,
//  )
//
//  private fun accessTokenProvider(authTokenService: AuthTokenService, clock: Clock): AccessTokenProvider {
//    return AndroidAccessTokenProvider(authTokenService, clock)
//  }
//
//  private fun testOkHttpClient(interceptor: Interceptor) = OkHttpClient
//    .Builder()
//    .addInterceptor(interceptor)
//    .build()
// }

/**
 * Without this, just doing .execute() makes the tests running with `runTest` in combination with having a datastore
 * running on their `backgroundScope` hang indefinitely. Using `enqueue` stops this problem.
 * Not sure about the details of this and can't find a public issue.
 * If removing this function suddenly makes tests still pass feel free to remove it in the future.
 */
private suspend fun Call.enqueueAndSuspendUntilCallbackIgnoringResponse() {
  suspendCancellableCoroutine<Unit> { cont ->
    enqueue(
      object : Callback {
        override fun onFailure(call: Call, e: IOException) {
          cont.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
          cont.resume(Unit)
        }
      },
    )
  }
}
