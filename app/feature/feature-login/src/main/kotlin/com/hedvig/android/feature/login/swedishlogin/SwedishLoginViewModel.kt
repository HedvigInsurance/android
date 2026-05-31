package com.hedvig.android.feature.login.swedishlogin

import androidx.lifecycle.SavedStateHandle
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.authlib.AuthRepository
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.SharingStarted

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
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
