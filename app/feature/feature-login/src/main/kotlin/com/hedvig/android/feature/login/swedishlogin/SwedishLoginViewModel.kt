package com.hedvig.android.feature.login.swedishlogin

import androidx.lifecycle.SavedStateHandle
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.authlib.AuthRepository
import kotlinx.coroutines.flow.SharingStarted

internal class SwedishLoginViewModel(
  authTokenService: AuthTokenService,
  authRepository: AuthRepository,
  demoManager: DemoManager,
  savedStateHandle: SavedStateHandle,
) : MoleculeViewModel<SwedishLoginEvent, SwedishLoginUiState>(
    SwedishLoginUiState(BankIdUiState.Loading, false),
    SwedishLoginPresenter(authTokenService, authRepository, demoManager, savedStateHandle),
    SharingStarted.WhileSubscribed(),
  )
