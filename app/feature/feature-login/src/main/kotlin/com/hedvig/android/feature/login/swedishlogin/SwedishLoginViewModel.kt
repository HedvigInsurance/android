package com.hedvig.android.feature.login.swedishlogin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.authlib.AuthRepository
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.SharingStarted

@AssistedInject
internal class SwedishLoginViewModel(
  authTokenService: AuthTokenService,
  authRepository: AuthRepository,
  demoManager: DemoManager,
  @Assisted savedStateHandle: SavedStateHandle,
) : MoleculeViewModel<SwedishLoginEvent, SwedishLoginUiState>(
    SwedishLoginUiState(BankIdUiState.Loading, false),
    SwedishLoginPresenter(authTokenService, authRepository, demoManager, savedStateHandle),
    SharingStarted.WhileSubscribed(),
  ) {
  @AssistedFactory
  @ViewModelAssistedFactoryKey(SwedishLoginViewModel::class)
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    override fun create(extras: CreationExtras): SwedishLoginViewModel = create(extras.createSavedStateHandle())

    fun create(
      @Assisted savedStateHandle: SavedStateHandle,
    ): SwedishLoginViewModel
  }
}
