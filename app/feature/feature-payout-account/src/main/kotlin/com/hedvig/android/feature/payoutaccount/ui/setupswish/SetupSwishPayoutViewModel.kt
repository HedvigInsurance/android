package com.hedvig.android.feature.payoutaccount.ui.setupswish

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.payoutaccount.data.SetupSwishPayoutUseCase
import com.hedvig.android.feature.payoutaccount.navigation.SelectPayoutMethodKey
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SetupSwishPayoutViewModel(
  setupSwishPayoutUseCase: SetupSwishPayoutUseCase,
  backstack: Backstack,
) : MoleculeViewModel<SetupSwishPayoutEvent, SetupSwishPayoutUiState>(
    SetupSwishPayoutUiState(TextFieldState(), false, null, false),
    SetupSwishPayoutPresenter(setupSwishPayoutUseCase, backstack),
  )

internal sealed interface SetupSwishPayoutEvent {
  data object Save : SetupSwishPayoutEvent

  data object ShowedSnackBar : SetupSwishPayoutEvent
}

internal data class SetupSwishPayoutUiState(
  val phoneNumberState: TextFieldState,
  val isLoading: Boolean,
  val errorMessage: ErrorMessage?,
  val showSuccessSnackBar: Boolean,
)

internal class SetupSwishPayoutPresenter(
  private val setupSwishPayoutUseCase: SetupSwishPayoutUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<SetupSwishPayoutEvent, SetupSwishPayoutUiState> {
  @Composable
  override fun MoleculePresenterScope<SetupSwishPayoutEvent>.present(
    lastState: SetupSwishPayoutUiState,
  ): SetupSwishPayoutUiState {
    val phoneNumberState = remember { lastState.phoneNumberState }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<ErrorMessage?>(null) }
    var showSuccessSnackBar by remember { mutableStateOf(false) }
    var saveIteration by remember { mutableStateOf<String?>(null) }

    val currentSave = saveIteration
    if (currentSave != null) {
      LaunchedEffect(currentSave) {
        isLoading = true
        errorMessage = null
        setupSwishPayoutUseCase.invoke(currentSave).fold(
          ifLeft = {
            isLoading = false
            errorMessage = it
            saveIteration = null
          },
          ifRight = {
            isLoading = false
            showSuccessSnackBar = true
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

        SetupSwishPayoutEvent.ShowedSnackBar -> {
          backstack.popUpTo<SelectPayoutMethodKey>(inclusive = true)
        }
      }
    }

    return SetupSwishPayoutUiState(
      phoneNumberState = phoneNumberState,
      isLoading = isLoading,
      errorMessage = errorMessage,
      showSuccessSnackBar = showSuccessSnackBar,
    )
  }
}
