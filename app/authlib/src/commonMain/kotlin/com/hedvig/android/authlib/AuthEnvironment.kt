package com.hedvig.android.authlib

public enum class AuthEnvironment {
  STAGING,
  PRODUCTION,
}

public val AuthEnvironment.baseUrl: String
  get() = when (this) {
    AuthEnvironment.STAGING -> "https://auth.dev.hedvigit.com"
    AuthEnvironment.PRODUCTION -> "https://auth.prod.hedvigit.com"
  }
