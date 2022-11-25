package com.hedvig.android.auth.test

import com.hedvig.android.auth.LoginStatus
import com.hedvig.android.auth.LoginStatusService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeLoginStatusService(
  override var isLoggedIn: Boolean = false,
) : LoginStatusService {
  override suspend fun getLoginStatus(): LoginStatus = error("Not implemented")
  override fun getLoginStatusAsFlow(): Flow<LoginStatus> = emptyFlow()
}
