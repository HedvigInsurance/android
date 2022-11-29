package com.hedvig.android.auth

import kotlinx.coroutines.flow.Flow

@Deprecated("Use AuthTokenService instead")
interface LoginStatusService {
  var isLoggedIn: Boolean
  suspend fun getLoginStatus(): LoginStatus
  fun getLoginStatusAsFlow(): Flow<LoginStatus>
}
