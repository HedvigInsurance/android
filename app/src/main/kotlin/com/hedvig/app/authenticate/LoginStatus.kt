package com.hedvig.app.authenticate

sealed class LoginStatus {
  object Onboarding : LoginStatus()
  object LoggedIn : LoginStatus()
}
