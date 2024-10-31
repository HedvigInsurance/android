package com.hedvig.android.feature.login.swedishlogin

import androidx.lifecycle.SavedStateHandle
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.molecule.android.MoleculeViewModel
import kotlinx.coroutines.flow.SharingStarted

internal class SwedishLoginViewModel(
  authTokenService: AuthTokenService,
  demoManager: DemoManager,
  savedStateHandle: SavedStateHandle,
) : MoleculeViewModel<SwedishLoginEvent, SwedishLoginUiState>(
    SwedishLoginUiState(BankIdUiState.Loading, false),
    SwedishLoginPresenter(authTokenService, demoManager, savedStateHandle),
    SharingStarted.WhileSubscribed(),
  )
