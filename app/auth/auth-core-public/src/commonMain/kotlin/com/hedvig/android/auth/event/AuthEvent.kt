package com.hedvig.android.auth.event

sealed interface AuthEvent {
  data class LoggedIn(
    val accessToken: String,
    val refreshToken: String,
  ) : AuthEvent

  data object LoggedOut : AuthEvent
}
