package com.hedvig.android.auth

import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.launch

interface LogoutUseCase {
  fun invoke()
}

internal class LogoutUseCaseImpl(
  private val authTokenService: AuthTokenService,
  private val applicationScope: ApplicationScope,
) : LogoutUseCase {
  override fun invoke() {
    logcat { "Logout usecase called" }
    applicationScope.launch { authTokenService.logoutAndInvalidateTokens() }
  }
}
