package com.hedvig.android.auth

import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.launch

interface LogoutUseCase {
  fun invoke()
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class LogoutUseCaseImpl(
  private val authTokenService: AuthTokenService,
  private val applicationScope: ApplicationScope,
) : LogoutUseCase {
  override fun invoke() {
    logcat { "Logout usecase called" }
    applicationScope.launch { authTokenService.logoutAndInvalidateTokens() }
  }
}
