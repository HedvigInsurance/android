package com.hedvig.android.feature.login.swedishlogin

import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.authlib.AuthRepository
import kotlinx.coroutines.flow.SharingStarted

internal class SwedishLoginViewModel(
  private val authTokenService: AuthTokenService,
  private val authRepository: AuthRepository,
) : MoleculeViewModel<SwedishLoginEvent, SwedishLoginUiState>(
  SwedishLoginUiState.Loading,
  SwedishLoginPresenter(authTokenService, authRepository),
  // SharingStarted.Lazily is very important for this case, since we do want to explicitly keep the flow of the auth
  // library alive even when the app goes to the background. On top of this, we also do want to start the exchange just
  // once, and not again when we come back to the app.
  SharingStarted.Lazily,
)
