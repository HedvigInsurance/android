package com.hedvig.android.feature.payin.account.ui.methoddetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.payin.account.data.GetPayinAccountUseCase
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.data.id
import com.hedvig.android.feature.payin.account.navigation.PayinMethodId
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class PayinMethodDetailsViewModel(
  @Assisted methodId: PayinMethodId,
  getPayinAccountUseCase: GetPayinAccountUseCase,
) : MoleculeViewModel<PayinMethodDetailsEvent, PayinMethodDetailsUiState>(
    initialState = PayinMethodDetailsUiState.Loading,
    presenter = PayinMethodDetailsPresenter(methodId, getPayinAccountUseCase),
  )

internal sealed interface PayinMethodDetailsEvent {
  data object Retry : PayinMethodDetailsEvent
}

internal sealed interface PayinMethodDetailsUiState {
  data object Loading : PayinMethodDetailsUiState

  data object Error : PayinMethodDetailsUiState

  data class Content(val method: PayinAccount, val chargingDay: Int?) : PayinMethodDetailsUiState
}

internal class PayinMethodDetailsPresenter(
  private val methodId: PayinMethodId,
  private val getPayinAccountUseCase: GetPayinAccountUseCase,
) : MoleculePresenter<PayinMethodDetailsEvent, PayinMethodDetailsUiState> {
  @Composable
  override fun MoleculePresenterScope<PayinMethodDetailsEvent>.present(
    lastState: PayinMethodDetailsUiState,
  ): PayinMethodDetailsUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var uiState by remember { mutableStateOf(lastState) }

    LaunchedEffect(loadIteration) {
      uiState = PayinMethodDetailsUiState.Loading
      getPayinAccountUseCase.invoke().fold(
        ifLeft = { uiState = PayinMethodDetailsUiState.Error },
        ifRight = { data ->
          val method = data.currentMethods.firstOrNull { it.id == methodId }
          // The method can be gone if it was disconnected on another device between listing and opening it.
          uiState = if (method == null) {
            PayinMethodDetailsUiState.Error
          } else {
            PayinMethodDetailsUiState.Content(method, data.chargingDay)
          }
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        PayinMethodDetailsEvent.Retry -> loadIteration++
      }
    }

    return uiState
  }
}
