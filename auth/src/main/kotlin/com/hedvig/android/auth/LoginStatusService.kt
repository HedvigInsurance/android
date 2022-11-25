package com.hedvig.android.auth

import kotlinx.coroutines.flow.Flow

interface LoginStatusService {
  var isLoggedIn: Boolean
  suspend fun getLoginStatus(): LoginStatus
  fun getLoginStatusAsFlow(): Flow<LoginStatus>
}
