package com.hedvig.android.auth

interface AuthenticationTokenService {
  var authenticationToken: String?
  var refreshToken: RefreshToken?
}
