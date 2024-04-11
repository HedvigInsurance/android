package com.hedvig.android.auth.event

interface AuthEventListener {
  /**
   * Called after the user was logged out and the auth tokens are now populated
   * This happens as of right now:
   * 1. When we get an `authorizationCode` from trying to login with BankID and we successfully exchange it for a real
   * set of tokens
   * 2. When we submit the OTP code from the OTP login method and we successfully get back an `authorizationCode` which
   * we successfully exchange it for a real set of tokens
   * 3. In the debug app when the impersonation receiver receives an exchange token it successfully exchanges it for a
   * real set of tokens
   */
  suspend fun loggedIn(accessToken: String) {}

  /**
   * Called after the user was logged out and the auth tokens were cleared.
   * This happens as of right now:
   * 1. When the logout button is clicked in the profile screen
   * 2. When we try to refresh our expired token but the refresh token also is expired or the request to refresh fails
   */
  suspend fun loggedOut() {}
}
