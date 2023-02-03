package com.hedvig.android.auth.event

interface AuthEventListener {
  suspend fun loggedIn()
  suspend fun loggedOut()
}
