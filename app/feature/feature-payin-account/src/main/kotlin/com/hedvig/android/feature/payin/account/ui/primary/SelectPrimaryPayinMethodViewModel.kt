package com.hedvig.android.feature.payin.account.ui.primary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.data.SetAsDefaultUseCase
import com.hedvig.android.feature.payin.account.data.provider
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SelectPrimaryPayinMethodViewModel(
  @Assisted currentMethods: List<PayinAccount>,
  setAsDefaultUseCase: SetAsDefaultUseCase,
) : MoleculeViewModel<SelectPrimaryPayinMethodEvent, SelectPrimaryPayinMethodUiState>(
    initialState = SelectPrimaryPayinMethodUiState(
      methods = currentMethods,
      selectedMethod = null,
    ),
    presenter = SelectPrimaryPayinMethodPresenter(setAsDefaultUseCase),
  )

internal sealed interface SelectPrimaryPayinMethodEvent {
  data class SelectMethod(val method: PayinAccount) : SelectPrimaryPayinMethodEvent

  data object ConfirmSelectedMethod : SelectPrimaryPayinMethodEvent
}

internal data class SelectPrimaryPayinMethodUiState(
  val methods: List<PayinAccount>,
  val selectedMethod: PayinAccount?,
  val isConfirming: Boolean = false,
  val errorMessage: String? = null,
  val hasSetPrimaryMethod: Boolean = false,
)

internal class SelectPrimaryPayinMethodPresenter(
  private val setAsDefaultUseCase: SetAsDefaultUseCase,
) : MoleculePresenter<SelectPrimaryPayinMethodEvent, SelectPrimaryPayinMethodUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectPrimaryPayinMethodEvent>.present(
    lastState: SelectPrimaryPayinMethodUiState,
  ): SelectPrimaryPayinMethodUiState {
    var selectedMethod by remember { mutableStateOf(lastState.selectedMethod) }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasSetPrimaryMethod by remember { mutableStateOf(false) }
    var methodToConfirm by remember { mutableStateOf<PayinAccount?>(null) }

    LaunchedEffect(methodToConfirm) {
      val method = methodToConfirm ?: return@LaunchedEffect
      isConfirming = true
      errorMessage = null
      setAsDefaultUseCase.invoke(method.provider).fold(
        ifLeft = {
          isConfirming = false
          methodToConfirm = null
          errorMessage = it.message
        },
        ifRight = {
          isConfirming = false
          methodToConfirm = null
          hasSetPrimaryMethod = true
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        is SelectPrimaryPayinMethodEvent.SelectMethod -> {
          selectedMethod = event.method
        }

        SelectPrimaryPayinMethodEvent.ConfirmSelectedMethod -> {
          if (!isConfirming) {
            methodToConfirm = selectedMethod
          }
        }
      }
    }

    return lastState.copy(
      selectedMethod = selectedMethod,
      isConfirming = isConfirming,
      errorMessage = errorMessage,
      hasSetPrimaryMethod = hasSetPrimaryMethod,
    )
  }
}
