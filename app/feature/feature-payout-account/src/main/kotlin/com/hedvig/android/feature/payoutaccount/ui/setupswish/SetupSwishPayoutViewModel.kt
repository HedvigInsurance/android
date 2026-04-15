package com.hedvig.android.feature.payoutaccount.ui.setupswish

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.payoutaccount.data.SetupSwishPayoutUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class SetupSwishPayoutViewModel(
  setupSwishPayoutUseCase: SetupSwishPayoutUseCase,
) : MoleculeViewModel<SetupSwishPayoutEvent, SetupSwishPayoutUiState>(
    SetupSwishPayoutUiState(TextFieldState(), false, null, false),
    SetupSwishPayoutPresenter(setupSwishPayoutUseCase),
  )

internal sealed interface SetupSwishPayoutEvent {
  data object Save : SetupSwishPayoutEvent
}

internal data class SetupSwishPayoutUiState(
  val phoneNumberState: TextFieldState,
  val isLoading: Boolean,
  val errorMessage: String?,
  val navigateBack: Boolean,
)

internal class SetupSwishPayoutPresenter(
  private val setupSwishPayoutUseCase: SetupSwishPayoutUseCase,
) : MoleculePresenter<SetupSwishPayoutEvent, SetupSwishPayoutUiState> {
  @Composable
  override fun MoleculePresenterScope<SetupSwishPayoutEvent>.present(
    lastState: SetupSwishPayoutUiState,
  ): SetupSwishPayoutUiState {
    val phoneNumberState = remember { lastState.phoneNumberState }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var navigateBack by remember { mutableStateOf(false) }
    var saveIteration by remember { mutableStateOf<String?>(null) }

    val currentSave = saveIteration
    if (currentSave != null) {
      LaunchedEffect(currentSave) {
        isLoading = true
        errorMessage = null
        setupSwishPayoutUseCase.invoke(currentSave).fold(
          ifLeft = {
            isLoading = false
            errorMessage = it.message ?: "Something went wrong, please try again"
            saveIteration = null
          },
          ifRight = {
            isLoading = false
            navigateBack = true
            saveIteration = null
          },
        )
      }
    }

    CollectEvents { event ->
      when (event) {
        SetupSwishPayoutEvent.Save -> {
          if (!isLoading) {
            saveIteration = phoneNumberState.text.toString()
          }
        }
      }
    }

    return SetupSwishPayoutUiState(
      phoneNumberState = phoneNumberState,
      isLoading = isLoading,
      errorMessage = errorMessage,
      navigateBack = navigateBack,
    )
  }
}
