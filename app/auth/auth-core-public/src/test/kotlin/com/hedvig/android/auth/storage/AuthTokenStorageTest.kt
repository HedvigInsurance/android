package com.hedvig.android.auth.storage

// TODO("todo does not work without authlib")
// internal class AuthTokenStorageTest {
//  @get:Rule
//  val testFolder = TemporaryFolder()
//
//  @get:Rule
//  val testLogcatLogger = TestLogcatLoggingRule()
//
//  @Test
//  fun `storing tokens in AuthTokenStorage persists their expiration time`() = runTest {
//    val clock = TestClock()
//    val authTokenStorage = authTokenStorage(clock)
//    val now = clock.now()
//
//    val expiryInSeconds = 5.minutes.inWholeSeconds
//    authTokenStorage.updateTokens(
//      AccessToken("", expiryInSeconds),
//      RefreshToken("", expiryInSeconds),
//    )
//
//    val (accessToken, refreshToken) = authTokenStorage.getTokens().filterNotNull().first()
//
//    val expectedTokenExpirationInSeconds = now + 5.minutes
//    assertThat(accessToken.expiryDate).isEqualTo(expectedTokenExpirationInSeconds)
//    assertThat(refreshToken.expiryDate).isEqualTo(expectedTokenExpirationInSeconds)
//  }
//
//  private fun TestScope.authTokenStorage(clock: Clock): AuthTokenStorage {
//    return AuthTokenStorage(
//      TestPreferencesDataStore(
//        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
//        coroutineScope = backgroundScope,
//      ),
//      clock,
//    )
//  }
// }
