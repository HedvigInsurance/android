package com.hedvig.android.feature.payments2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.payments2.data.AddDiscountUseCase
import com.hedvig.android.feature.payments2.data.GetUpcomingPaymentUseCase
import com.hedvig.android.feature.payments2.data.PaymentOverview
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class PaymentOverviewPresenter(
  val getUpcomingPaymentUseCase: GetUpcomingPaymentUseCase,
  val addDiscountUseCase: AddDiscountUseCase,
) : MoleculePresenter<PaymentEvent, OverViewUiState> {
  @Composable
  override fun MoleculePresenterScope<PaymentEvent>.present(lastState: OverViewUiState): OverViewUiState {
    var paymentUiState: OverViewUiState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var addedDiscount by remember { mutableStateOf<String?>(null) }

    CollectEvents { event ->
      when (event) {
        PaymentEvent.Retry -> loadIteration++
        is PaymentEvent.OnSubmitDiscountCode -> addedDiscount = event.code
        PaymentEvent.DismissBottomSheet -> {
          addedDiscount = null
          paymentUiState = paymentUiState.copy(
            showAddDiscountBottomSheet = false,
            discountError = null,
          )
        }

        PaymentEvent.ShowBottomSheet -> {
          paymentUiState = paymentUiState.copy(
            showAddDiscountBottomSheet = true,
          )
        }
      }
    }

    LaunchedEffect(loadIteration) {
      paymentUiState = paymentUiState.copy(isLoadingPaymentOverView = true)

      getUpcomingPaymentUseCase.invoke().fold(
        ifLeft = {
          paymentUiState = paymentUiState.copy(
            error = it.message,
            isLoadingPaymentOverView = false,
          )
        },
        ifRight = {
          paymentUiState = paymentUiState.copy(
            paymentOverview = it,
            error = null,
            isLoadingPaymentOverView = false,
          )
        },
      )
    }

    LaunchedEffect(addedDiscount) {
      addedDiscount?.let {
        paymentUiState = paymentUiState.copy(
          discountError = null,
          isAddingDiscount = true,
        )

        addDiscountUseCase.invoke(it).fold(
          ifLeft = { errorMessage ->
            addedDiscount = null
            paymentUiState = paymentUiState.copy(
              discountError = errorMessage.message,
              isAddingDiscount = false,
            )
          },
          ifRight = {
            addedDiscount = null
            paymentUiState = paymentUiState.copy(
              showAddDiscountBottomSheet = false,
              isAddingDiscount = false,
            )
          },
        )
      }
    }

    return paymentUiState
  }
}

internal sealed interface PaymentEvent {
  data object Retry : PaymentEvent
  data object DismissBottomSheet : PaymentEvent
  data object ShowBottomSheet : PaymentEvent
  data class OnSubmitDiscountCode(val code: String) : PaymentEvent
}

internal data class OverViewUiState(
  val paymentOverview: PaymentOverview? = null,
  val discountError: String? = null,
  val error: String? = null,
  val showAddDiscountBottomSheet: Boolean = false,
  val isLoadingPaymentOverView: Boolean = true,
  val isAddingDiscount: Boolean = false,
)
