package com.hedvig.android.feature.payments.ui.discounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.payments.data.DiscountedContract
import com.hedvig.android.feature.payments.data.GetDiscountsOverviewUseCase
import com.hedvig.android.feature.payments.overview.data.ForeverInformation
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class DiscountsPresenter(
  private val getDiscountsOverviewUseCase: GetDiscountsOverviewUseCase,
) : MoleculePresenter<DiscountsEvent, DiscountsUiState> {
  @Composable
  override fun MoleculePresenterScope<DiscountsEvent>.present(lastState: DiscountsUiState): DiscountsUiState {
    var paymentUiState: DiscountsUiState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        DiscountsEvent.Retry -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      paymentUiState = if (loadIteration != 0) {
        paymentUiState.copy(isRetrying = true)
      } else {
        paymentUiState.copy(isLoadingPaymentOverView = true)
      }
      getDiscountsOverviewUseCase.invoke().fold(
        ifLeft = {
          paymentUiState = paymentUiState.copy(
            error = true,
            isLoadingPaymentOverView = false,
            isRetrying = false,
          )
        },
        ifRight = { discountsOverview ->
          paymentUiState = paymentUiState.copy(
            discountedContracts = discountsOverview.discountedContracts,
            foreverInformation = discountsOverview.foreverInformation,
            error = null,
            isLoadingPaymentOverView = false,
            isRetrying = false,
          )
        },
      )
    }

    return paymentUiState
  }
}

internal sealed interface DiscountsEvent {
  data object Retry : DiscountsEvent
}

internal data class DiscountsUiState(
  val foreverInformation: ForeverInformation?,
  val discountedContracts: Set<DiscountedContract>,
  val error: Boolean? = null,
  val isLoadingPaymentOverView: Boolean = true,
  val isRetrying: Boolean = false,
)
