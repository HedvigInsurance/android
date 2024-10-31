package com.hedvig.android.feature.login.swedishlogin

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

@OptIn(SavedStateHandleSaveableApi::class)
internal class SwedishLoginPresenter(
  private val authTokenService: AuthTokenService,
  private val demoManager: DemoManager,
  private val savedStateHandle: SavedStateHandle,
) : MoleculePresenter<SwedishLoginEvent, SwedishLoginUiState> {
  @Composable
  override fun MoleculePresenterScope<SwedishLoginEvent>.present(lastState: SwedishLoginUiState): SwedishLoginUiState {
    return SwedishLoginUiState(BankIdUiState.Loading, false)
  }
}

@Composable
private fun isLoggedInAsState(demoManager: DemoManager, authTokenService: AuthTokenService): State<Boolean> {
  val isDemoMode by demoManager.isDemoMode().collectAsState(false)
  val isLoggedIn by authTokenService.authStatus.collectAsState()
  return remember {
    derivedStateOf {
      isDemoMode || isLoggedIn is AuthStatus.LoggedIn
    }
  }
}

internal sealed interface SwedishLoginEvent {
  data object Retry : SwedishLoginEvent

  data object DidOpenBankIDApp : SwedishLoginEvent

  data object StartDemoMode : SwedishLoginEvent
}

internal data class SwedishLoginUiState(
  val bankIdUiState: BankIdUiState,
  val navigateToLoginScreen: Boolean,
)

internal sealed interface BankIdUiState {
  data class HandlingBankId(
    val statusMessage: String,
    val autoStartToken: AutoStartToken,
    val bankIdLiveQrCodeData: BankIdLiveQrCodeData?,
    val bankIdAppOpened: Boolean,
    val allowOpeningBankId: Boolean,
  ) : BankIdUiState {
    @JvmInline
    value class AutoStartToken(val token: String) {
      // The Uri which opens the BankId app while also passing in the right autoStartUrl
      val bankIdUri: Uri
        get() = Uri.parse("https://app.bankid.com/?autostarttoken=$token&redirect=null")
    }

    @JvmInline
    value class BankIdLiveQrCodeData(val data: String)
  }

  data object Loading : BankIdUiState

  data object LoggedIn : BankIdUiState

  data class BankIdError(val message: String) : BankIdUiState

  data object StartLoginAttemptFailed : BankIdUiState
}
