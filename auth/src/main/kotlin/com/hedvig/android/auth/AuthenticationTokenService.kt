package com.hedvig.android.auth

import com.hedvig.authlib.RefreshToken

interface AuthenticationTokenService {
  var authenticationToken: String?
  var refreshToken: RefreshToken?
}
