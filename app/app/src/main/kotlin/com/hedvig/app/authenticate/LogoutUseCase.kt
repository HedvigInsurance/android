package com.hedvig.app.authenticate

import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.core.appreview.SelfServiceCompletedEventStore
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.launch

internal class LogoutUseCaseImpl(
  private val authTokenService: AuthTokenService,
  private val selfServiceCompletedEventStore: SelfServiceCompletedEventStore,
  private val applicationScope: ApplicationScope,
  private val demoManager: DemoManager,
) : LogoutUseCase {
  override fun invoke() {
    logcat { "Logout usecase called" }
    applicationScope.launch { authTokenService.logoutAndInvalidateTokens() }
    applicationScope.launch { selfServiceCompletedEventStore.resetSelfServiceCompletions() }
    applicationScope.launch { demoManager.setDemoMode(false) }
  }
}
