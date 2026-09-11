package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.payin.account.data.SetupSwishPayinUseCase
import com.hedvig.android.feature.payin.account.data.SetupSwishResponse
import com.hedvig.android.feature.payin.account.data.SwishSetupOrder
import com.hedvig.android.feature.payin.account.data.order
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SetupSwishPayinViewModel(
  setupSwishPayoutUseCase: SetupSwishPayinUseCase,
) : MoleculeViewModel<SetupSwishPayoutEvent, SetupSwishPayoutUiState>(
    SetupSwishPayoutUiState(
      phoneNumber = "",
      isLoading = false,
      error = null,
      showSuccessSnackBar = false,
      orderToApprove = null,
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
  /** Set once the setup needs approving in the Swish app, which the caller moves on to. */
  val orderToApprove: SwishSetupOrder? = null,
  val phoneNumber: String,
  val isLoading: Boolean,
  val error: ErrorMessage?,
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
    var saveIteration by remember { mutableStateOf<String?>(null) }
    var orderToApprove by remember { mutableStateOf<SwishSetupOrder?>(null) }

    val currentSave = saveIteration
    if (currentSave != null) {
      LaunchedEffect(currentSave) {
        isLoading = true
        errorMessage = null
        setupSwishPayoutUseCase.invoke(phoneNumberState).fold(
          ifLeft = {
            isLoading = false
            errorMessage = it
            saveIteration = null
          },
          ifRight = { result ->
            isLoading = false
            saveIteration = null
            val order = result.order
            when {
              order != null -> orderToApprove = order

              // A setup that needs no approving is already done, so there is nothing to wait on.
              result is SetupSwishResponse.Success -> showSuccessSnackBar = true

              else -> errorMessage = (result as? SetupSwishResponse.Failure)?.error ?: ErrorMessage()
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
      orderToApprove = orderToApprove,
    )
  }
}
