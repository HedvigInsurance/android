package com.hedvig.android.feature.payin.account.ui.selectmethod

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import octopus.type.MemberPaymentProvider

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class ConnectPayinMethodViewModel(
  @Assisted availableProviders: List<String>,
) : MoleculeViewModel<SelectPayinMethodEvent, ConnectPayinMethodUiState>(
    initialState = ConnectPayinMethodUiState(
      availableProviders = availableProviders.map(MemberPaymentProvider::safeValueOf),
      selectedProvider = null,
    ),
    presenter = SelectPayinMethodPresenter(),
  )

internal sealed interface SelectPayinMethodEvent {
  data class SelectProvider(val provider: MemberPaymentProvider) : SelectPayinMethodEvent
}

internal data class ConnectPayinMethodUiState(
  val availableProviders: List<MemberPaymentProvider>,
  val selectedProvider: MemberPaymentProvider?,
)

internal class SelectPayinMethodPresenter :
  MoleculePresenter<SelectPayinMethodEvent, ConnectPayinMethodUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectPayinMethodEvent>.present(
    lastState: ConnectPayinMethodUiState,
  ): ConnectPayinMethodUiState {
    var selectedProvider by remember { mutableStateOf(lastState.selectedProvider) }

    CollectEvents { event ->
      when (event) {
        is SelectPayinMethodEvent.SelectProvider -> selectedProvider = event.provider
      }
    }

    return lastState.copy(selectedProvider = selectedProvider)
  }
}
