package com.hedvig.android.feature.payoutaccount.ui.primary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.payoutaccount.data.GetPayoutAccountUseCase
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount
import com.hedvig.android.feature.payoutaccount.data.SetAsDefaultPayoutUseCase
import com.hedvig.android.feature.payoutaccount.data.provider
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SelectPrimaryPayoutMethodViewModel(
  getPayoutAccountUseCase: GetPayoutAccountUseCase,
  setAsDefaultPayoutUseCase: SetAsDefaultPayoutUseCase,
) : MoleculeViewModel<SelectPrimaryPayoutMethodEvent, SelectPrimaryPayoutMethodUiState>(
    initialState = SelectPrimaryPayoutMethodUiState.Loading,
    presenter = SelectPrimaryPayoutMethodPresenter(getPayoutAccountUseCase, setAsDefaultPayoutUseCase),
  )

internal sealed interface SelectPrimaryPayoutMethodEvent {
  data class SelectMethod(val method: PayoutAccount) : SelectPrimaryPayoutMethodEvent

  data object ConfirmSelectedMethod : SelectPrimaryPayoutMethodEvent

  data object Retry : SelectPrimaryPayoutMethodEvent
}

internal sealed interface SelectPrimaryPayoutMethodUiState {
  data object Loading : SelectPrimaryPayoutMethodUiState

  data object Failed : SelectPrimaryPayoutMethodUiState

  data class Content(
    val methods: List<PayoutAccount>,
    val selectedMethod: PayoutAccount?,
    val isConfirming: Boolean = false,
    val errorMessage: String? = null,
    val hasSetPrimaryMethod: Boolean = false,
  ) : SelectPrimaryPayoutMethodUiState
}

internal class SelectPrimaryPayoutMethodPresenter(
  private val getPayoutAccountUseCase: GetPayoutAccountUseCase,
  private val setAsDefaultPayoutUseCase: SetAsDefaultPayoutUseCase,
) : MoleculePresenter<SelectPrimaryPayoutMethodEvent, SelectPrimaryPayoutMethodUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectPrimaryPayoutMethodEvent>.present(
    lastState: SelectPrimaryPayoutMethodUiState,
  ): SelectPrimaryPayoutMethodUiState {
    val lastContent = lastState as? SelectPrimaryPayoutMethodUiState.Content
    var methods by remember { mutableStateOf(lastContent?.methods) }
    var loadFailed by remember { mutableStateOf(false) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var selectedMethod by remember { mutableStateOf(lastContent?.selectedMethod) }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasSetPrimaryMethod by remember { mutableStateOf(lastContent?.hasSetPrimaryMethod ?: false) }
    var methodToConfirm by remember { mutableStateOf<PayoutAccount?>(null) }

    // Methods already on screen stay there if the refetch fails, so a restart never drops a usable list.
    LaunchedEffect(loadIteration) {
      getPayoutAccountUseCase.invoke().fold(
        ifLeft = {
          if (methods == null) loadFailed = true
        },
        ifRight = { data ->
          methods = data.currentMethods
          selectedMethod = selectedMethod?.let { selected ->
            data.currentMethods.firstOrNull { it.provider == selected.provider }
          }
        },
      )
    }

    LaunchedEffect(methodToConfirm) {
      val method = methodToConfirm ?: return@LaunchedEffect
      isConfirming = true
      errorMessage = null
      setAsDefaultPayoutUseCase.invoke(method.provider).fold(
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
        is SelectPrimaryPayoutMethodEvent.SelectMethod -> {
          selectedMethod = event.method
        }

        SelectPrimaryPayoutMethodEvent.ConfirmSelectedMethod -> {
          if (!isConfirming) {
            methodToConfirm = selectedMethod
          }
        }

        SelectPrimaryPayoutMethodEvent.Retry -> {
          loadFailed = false
          loadIteration++
        }
      }
    }

    val currentMethods = methods
    return when {
      currentMethods != null -> SelectPrimaryPayoutMethodUiState.Content(
        methods = currentMethods,
        selectedMethod = selectedMethod,
        isConfirming = isConfirming,
        errorMessage = errorMessage,
        hasSetPrimaryMethod = hasSetPrimaryMethod,
      )

      loadFailed -> SelectPrimaryPayoutMethodUiState.Failed

      else -> SelectPrimaryPayoutMethodUiState.Loading
    }
  }
}
