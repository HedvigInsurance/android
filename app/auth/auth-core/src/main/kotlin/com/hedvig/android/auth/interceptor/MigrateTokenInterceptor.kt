package com.hedvig.android.auth.interceptor

import com.hedvig.android.auth.AuthTokenServiceProvider
import com.hedvig.android.auth.storage.SharedPreferencesAuthenticationTokenService
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/** Migration from old auth api to oauth
Read old authentication token from shared preferences and update
using new oauth endpoints if exists
 */
class MigrateTokenInterceptor(
  private val authTokenServiceProvider: AuthTokenServiceProvider,
  private val sharedPreferencesAuthenticationTokenService: SharedPreferencesAuthenticationTokenService,
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val oldToken = sharedPreferencesAuthenticationTokenService.authenticationToken
    return if (oldToken != null) {
      runBlocking {
        authTokenServiceProvider.provide().migrateFromToken(oldToken)
        sharedPreferencesAuthenticationTokenService.authenticationToken = null
        chain.proceed(chain.request())
      }
    } else {
      chain.proceed(chain.request())
    }
  }
}
