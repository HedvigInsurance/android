package com.hedvig.android.feature.payin.account.ui.primary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.data.paying.member.provider
import com.hedvig.android.feature.payin.account.data.GetPayinAccountUseCase
import com.hedvig.android.feature.payin.account.data.SetAsDefaultUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SelectPrimaryPayinMethodViewModel(
  getPayinAccountUseCase: GetPayinAccountUseCase,
  setAsDefaultUseCase: SetAsDefaultUseCase,
) : MoleculeViewModel<SelectPrimaryPayinMethodEvent, SelectPrimaryPayinMethodUiState>(
    initialState = SelectPrimaryPayinMethodUiState.Loading,
    presenter = SelectPrimaryPayinMethodPresenter(getPayinAccountUseCase, setAsDefaultUseCase),
  )

internal sealed interface SelectPrimaryPayinMethodEvent {
  data class SelectMethod(val method: PayinAccount) : SelectPrimaryPayinMethodEvent

  data object ConfirmSelectedMethod : SelectPrimaryPayinMethodEvent

  data object Retry : SelectPrimaryPayinMethodEvent
}

internal sealed interface SelectPrimaryPayinMethodUiState {
  data object Loading : SelectPrimaryPayinMethodUiState

  data object Failed : SelectPrimaryPayinMethodUiState

  data class Content(
    val methods: List<PayinAccount>,
    val selectedMethod: PayinAccount?,
    val isConfirming: Boolean = false,
    val errorMessage: String? = null,
    val hasSetPrimaryMethod: Boolean = false,
  ) : SelectPrimaryPayinMethodUiState
}

internal class SelectPrimaryPayinMethodPresenter(
  private val getPayinAccountUseCase: GetPayinAccountUseCase,
  private val setAsDefaultUseCase: SetAsDefaultUseCase,
) : MoleculePresenter<SelectPrimaryPayinMethodEvent, SelectPrimaryPayinMethodUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectPrimaryPayinMethodEvent>.present(
    lastState: SelectPrimaryPayinMethodUiState,
  ): SelectPrimaryPayinMethodUiState {
    val lastContent = lastState as? SelectPrimaryPayinMethodUiState.Content
    var methods by remember { mutableStateOf(lastContent?.methods) }
    var loadFailed by remember { mutableStateOf(false) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var selectedMethod by remember { mutableStateOf(lastContent?.selectedMethod) }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasSetPrimaryMethod by remember { mutableStateOf(lastContent?.hasSetPrimaryMethod ?: false) }
    var methodToConfirm by remember { mutableStateOf<PayinAccount?>(null) }

    // Methods already on screen stay there if the refetch fails, so a restart never drops a usable list.
    LaunchedEffect(loadIteration) {
      getPayinAccountUseCase.invoke().fold(
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

        SelectPrimaryPayinMethodEvent.Retry -> {
          loadFailed = false
          loadIteration++
        }
      }
    }

    val currentMethods = methods
    return when {
      currentMethods != null -> SelectPrimaryPayinMethodUiState.Content(
        methods = currentMethods,
        selectedMethod = selectedMethod,
        isConfirming = isConfirming,
        errorMessage = errorMessage,
        hasSetPrimaryMethod = hasSetPrimaryMethod,
      )

      loadFailed -> SelectPrimaryPayinMethodUiState.Failed

      else -> SelectPrimaryPayinMethodUiState.Loading
    }
  }
}
