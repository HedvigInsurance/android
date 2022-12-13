package com.hedvig.android.auth

sealed class LoginStatus {
  object Onboarding : LoginStatus()
  object LoggedIn : LoginStatus()
}
