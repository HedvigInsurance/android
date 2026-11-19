package com.hedvig.android.feature.payin.account.ui.selectmethod

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.feature.payin.account.data.GetPayinAccountUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SelectPayinMethodViewModel(
  getPayinAccountUseCase: GetPayinAccountUseCase,
) : MoleculeViewModel<SelectPayinMethodEvent, SelectPayinMethodUiState>(
    initialState = SelectPayinMethodUiState.Loading,
    presenter = SelectPayinMethodPresenter(getPayinAccountUseCase),
  )

internal sealed interface SelectPayinMethodEvent {
  data class SelectProvider(val provider: PaymentProvider) : SelectPayinMethodEvent

  data object Retry : SelectPayinMethodEvent
}

internal sealed interface SelectPayinMethodUiState {
  data object Loading : SelectPayinMethodUiState

  data object Error : SelectPayinMethodUiState

  data class Content(
    val availableProviders: List<PaymentProvider>,
    val selectedProvider: PaymentProvider? = null,
  ) : SelectPayinMethodUiState
}

internal class SelectPayinMethodPresenter(
  private val getPayinAccountUseCase: GetPayinAccountUseCase,
) : MoleculePresenter<SelectPayinMethodEvent, SelectPayinMethodUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectPayinMethodEvent>.present(
    lastState: SelectPayinMethodUiState,
  ): SelectPayinMethodUiState {
    var uiState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      if (uiState is SelectPayinMethodUiState.Content) return@LaunchedEffect
      uiState = SelectPayinMethodUiState.Loading
      getPayinAccountUseCase.invoke().fold(
        ifLeft = { uiState = SelectPayinMethodUiState.Error },
        ifRight = { data ->
          uiState = SelectPayinMethodUiState.Content(availableProviders = data.availablePayinMethods)
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        is SelectPayinMethodEvent.SelectProvider -> {
          val state = uiState
          if (state is SelectPayinMethodUiState.Content) {
            uiState = state.copy(selectedProvider = event.provider)
          }
        }

        SelectPayinMethodEvent.Retry -> {
          loadIteration++
        }
      }
    }

    return uiState
  }
}
