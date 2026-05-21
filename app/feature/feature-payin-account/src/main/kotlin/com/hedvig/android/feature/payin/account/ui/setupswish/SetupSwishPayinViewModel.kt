package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.payin.account.data.SetupSwishPayinUseCase
import com.hedvig.android.feature.payin.account.data.SetupSwishResponse
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class SetupSwishPayinViewModel(
  setupSwishPayoutUseCase: SetupSwishPayinUseCase,
) : MoleculeViewModel<SetupSwishPayoutEvent, SetupSwishPayoutUiState>(
    SetupSwishPayoutUiState(
      phoneNumber = "",
      isLoading = false,
      error = null,
      showSuccessSnackBar = false,
      successUrl = null,
    ),
    SetupSwishPayoutPresenter(setupSwishPayoutUseCase),
  )

internal sealed interface SetupSwishPayoutEvent {
  data object Save : SetupSwishPayoutEvent

  data object ShowedSnackBar : SetupSwishPayoutEvent

  data class UpdateText(val newText: String) : SetupSwishPayoutEvent
}

internal data class SetupSwishPayoutUiState(
  val showSuccessSnackBar: Boolean,
  val successUrl: String? = null,
  val phoneNumber: String,
  val isLoading: Boolean,
  val error: ErrorMessage?,
  val resultIsPending: Boolean = false,
)

internal class SetupSwishPayoutPresenter(
  private val setupSwishPayoutUseCase: SetupSwishPayinUseCase,
) : MoleculePresenter<SetupSwishPayoutEvent, SetupSwishPayoutUiState> {
  @Composable
  override fun MoleculePresenterScope<SetupSwishPayoutEvent>.present(
    lastState: SetupSwishPayoutUiState,
  ): SetupSwishPayoutUiState {
    var phoneNumberState by remember { mutableStateOf(lastState.phoneNumber) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<ErrorMessage?>(null) }
    var showSuccessSnackBar by remember { mutableStateOf(false) }
    var resultIsPending by remember { mutableStateOf(false) }
    var saveIteration by remember { mutableStateOf<String?>(null) }
    var urlToRedirect by remember { mutableStateOf<String?>(null) }
    var uiState by remember { mutableStateOf(lastState) }

    val currentSave = saveIteration
    if (currentSave != null) {
      LaunchedEffect(currentSave) {
        isLoading = true
        errorMessage = null
        resultIsPending = false
        setupSwishPayoutUseCase.invoke(phoneNumberState).fold(
          ifLeft = {
            isLoading = false
            errorMessage = it
            saveIteration = null
          },
          ifRight = { result ->
            isLoading = false
            saveIteration = null
            when (result) {
              is SetupSwishResponse.Failure -> {
                errorMessage = result.error
              }

              is SetupSwishResponse.Pending -> {
                urlToRedirect = result.url
                resultIsPending = true
              }

              is SetupSwishResponse.Success -> {
                showSuccessSnackBar = true
                urlToRedirect = result.url
              }
            }
          },
        )
      }
    }

    CollectEvents { event ->
      when (event) {
        SetupSwishPayoutEvent.Save -> {
          if (!isLoading) {
            saveIteration = phoneNumberState
          }
        }

        SetupSwishPayoutEvent.ShowedSnackBar -> {
          showSuccessSnackBar = false
        }

        is SetupSwishPayoutEvent.UpdateText -> {
          phoneNumberState = event.newText
        }
      }
    }

    return SetupSwishPayoutUiState(
      phoneNumber = phoneNumberState,
      isLoading = isLoading,
      error = errorMessage,
      showSuccessSnackBar = showSuccessSnackBar,
      successUrl = urlToRedirect,
    )
  }
}
