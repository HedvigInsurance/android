package com.hedvig.android.feature.payin.account.ui.setupinvoice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.payin.account.data.SetupInvoicePayinUseCase
import com.hedvig.android.feature.payin.account.navigation.SelectPayinMethodKey
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SetupInvoicePayinViewModel(
  setupInvoicePayinUseCase: SetupInvoicePayinUseCase,
  backstack: Backstack,
) : MoleculeViewModel<SetupInvoicePayinEvent, SetupInvoicePayinUiState>(
    SetupInvoicePayinUiState(false, null, false),
    SetupInvoicePayinPresenter(setupInvoicePayinUseCase, backstack),
  )

internal sealed interface SetupInvoicePayinEvent {
  data object Connect : SetupInvoicePayinEvent

  data object ShowedSnackBar : SetupInvoicePayinEvent
}

internal data class SetupInvoicePayinUiState(
  val isLoading: Boolean,
  val errorMessage: String?,
  val showSuccessSnackBar: Boolean,
)

internal class SetupInvoicePayinPresenter(
  private val setupInvoicePayinUseCase: SetupInvoicePayinUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<SetupInvoicePayinEvent, SetupInvoicePayinUiState> {
  @Composable
  override fun MoleculePresenterScope<SetupInvoicePayinEvent>.present(
    lastState: SetupInvoicePayinUiState,
  ): SetupInvoicePayinUiState {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessSnackBar by remember { mutableStateOf(false) }
    var connectIteration by remember { mutableIntStateOf(0) }
    var shouldConnect by remember { mutableStateOf(false) }

    if (shouldConnect) {
      LaunchedEffect(connectIteration) {
        isLoading = true
        errorMessage = null
        setupInvoicePayinUseCase.invoke().fold(
          ifLeft = {
            isLoading = false
            errorMessage = it.message ?: "Something went wrong, please try again"
            shouldConnect = false
          },
          ifRight = {
            isLoading = false
            showSuccessSnackBar = true
            shouldConnect = false
          },
        )
      }
    }

    CollectEvents { event ->
      when (event) {
        SetupInvoicePayinEvent.Connect -> {
          if (!isLoading) {
            shouldConnect = true
            connectIteration++
          }
        }

        SetupInvoicePayinEvent.ShowedSnackBar -> {
          backstack.popUpTo<SelectPayinMethodKey>(inclusive = true)
        }
      }
    }

    return SetupInvoicePayinUiState(
      isLoading = isLoading,
      errorMessage = errorMessage,
      showSuccessSnackBar = showSuccessSnackBar,
    )
  }
}
