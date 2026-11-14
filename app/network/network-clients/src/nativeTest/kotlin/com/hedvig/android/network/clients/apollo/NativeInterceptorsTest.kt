package com.hedvig.android.network.clients.apollo

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.token.AuthTokens
import com.hedvig.android.authlib.AccessToken
import com.hedvig.android.authlib.RefreshToken
import com.hedvig.android.core.demomode.DemoManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NativeInterceptorsTest {
  @Test
  fun `on native only the logging interceptor is added`() {
    val apolloClient = ApolloClient.Builder()
      .serverUrl("https://example.test")
      .addHedvigInterceptors(FakeAuthTokenService(), FakeDemoManager())
      .build()

    assertEquals(listOf("LoggingInterceptor"), apolloClient.interceptors.map { it::class.simpleName })
  }
}

private class FakeAuthTokenService : AuthTokenService {
  override val authStatus: StateFlow<AuthStatus?> = MutableStateFlow(null)

  override suspend fun getTokens(): AuthTokens? = null

  override suspend fun refreshAndGetAccessToken(): AccessToken? = null

  override suspend fun loginWithTokens(accessToken: AccessToken, refreshToken: RefreshToken) {}

  override suspend fun logoutAndInvalidateTokens() {}
}

private class FakeDemoManager : DemoManager {
  override fun isDemoMode(): Flow<Boolean> = MutableStateFlow(false)

  override suspend fun setDemoMode(demoMode: Boolean) {}
}
