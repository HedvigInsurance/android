package com.hedvig.android.auth.event

sealed interface AuthEvent {
  data class LoggedIn(val accessToken: String) : AuthEvent
  data object LoggedOut : AuthEvent
}
