package com.hedvig.android.feature.login.swedishlogin

import androidx.lifecycle.SavedStateHandle
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.authlib.AuthRepository
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SwedishLoginViewModel(
  authTokenService: AuthTokenService,
  authRepository: AuthRepository,
  demoManager: DemoManager,
  @Assisted savedStateHandle: SavedStateHandle,
) : MoleculeViewModel<SwedishLoginEvent, SwedishLoginUiState>(
    SwedishLoginUiState(BankIdUiState.Loading, false),
    SwedishLoginPresenter(authTokenService, authRepository, demoManager, savedStateHandle),
    SharingStarted.WhileSubscribed(),
  )
