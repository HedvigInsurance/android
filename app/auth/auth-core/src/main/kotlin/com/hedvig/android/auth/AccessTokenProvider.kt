package com.hedvig.android.auth

/**
 * To be used in both Okhttp and ktor interceptors to append the access token to the header, and refresh it if there's
 * such a need.
 */
fun interface AccessTokenProvider {
  suspend fun provide(): String?
}
