package com.hedvig.android.feature.payoutaccount.ui.setupinvoice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.payoutaccount.data.SetupInvoicePayoutUseCase
import com.hedvig.android.feature.payoutaccount.navigation.SelectPayoutMethodKey
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SetupInvoicePayoutViewModel(
  setupInvoicePayoutUseCase: SetupInvoicePayoutUseCase,
  backstack: Backstack,
) : MoleculeViewModel<SetupInvoicePayoutEvent, SetupInvoicePayoutUiState>(
    SetupInvoicePayoutUiState(false, null, false),
    SetupInvoicePayoutPresenter(setupInvoicePayoutUseCase, backstack),
  )

internal sealed interface SetupInvoicePayoutEvent {
  data object Connect : SetupInvoicePayoutEvent

  data object ShowedSnackBar : SetupInvoicePayoutEvent
}

internal data class SetupInvoicePayoutUiState(
  val isLoading: Boolean,
  val errorMessage: String?,
  val showSuccessSnackBar: Boolean,
)

internal class SetupInvoicePayoutPresenter(
  private val setupInvoicePayoutUseCase: SetupInvoicePayoutUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<SetupInvoicePayoutEvent, SetupInvoicePayoutUiState> {
  @Composable
  override fun MoleculePresenterScope<SetupInvoicePayoutEvent>.present(
    lastState: SetupInvoicePayoutUiState,
  ): SetupInvoicePayoutUiState {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessSnackBar by remember { mutableStateOf(false) }
    var connectIteration by remember { mutableIntStateOf(0) }
    var shouldConnect by remember { mutableStateOf(false) }

    if (shouldConnect) {
      LaunchedEffect(connectIteration) {
        isLoading = true
        errorMessage = null
        setupInvoicePayoutUseCase.invoke().fold(
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
        SetupInvoicePayoutEvent.Connect -> {
          if (!isLoading) {
            shouldConnect = true
            connectIteration++
          }
        }

        SetupInvoicePayoutEvent.ShowedSnackBar -> {
          backstack.popUpTo<SelectPayoutMethodKey>(inclusive = true)
        }
      }
    }

    return SetupInvoicePayoutUiState(
      isLoading = isLoading,
      errorMessage = errorMessage,
      showSuccessSnackBar = showSuccessSnackBar,
    )
  }
}
