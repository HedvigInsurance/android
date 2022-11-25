package com.hedvig.app.authenticate

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeLoginStatusService(
  override var isLoggedIn: Boolean = false,
) : LoginStatusService {
  override suspend fun getLoginStatus(): LoginStatus = error("Not implemented")
  override fun observeIsLoggedIn(): Flow<LoginStatus> = emptyFlow()
}
