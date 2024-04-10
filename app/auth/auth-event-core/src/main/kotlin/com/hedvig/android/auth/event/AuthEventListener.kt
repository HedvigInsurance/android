package com.hedvig.android.auth.event

interface AuthEventListener {
  suspend fun loggedIn(accessToken: String) {}

  suspend fun loggedOut() {}
}
