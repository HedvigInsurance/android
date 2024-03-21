package com.hedvig.android.feature.payments.discounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.overview.data.AddDiscountUseCase
import com.hedvig.android.feature.payments.overview.data.ForeverInformation
import com.hedvig.android.feature.payments.overview.data.GetPaymentOverviewDataUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class DiscountsPresenter(
  val getPaymentOverviewDataUseCase: Provider<GetPaymentOverviewDataUseCase>,
  val addDiscountUseCase: AddDiscountUseCase,
) : MoleculePresenter<DiscountsEvent, DiscountsUiState> {
  @Composable
  override fun MoleculePresenterScope<DiscountsEvent>.present(lastState: DiscountsUiState): DiscountsUiState {
    var paymentUiState: DiscountsUiState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var addedDiscount by remember { mutableStateOf<String?>(null) }

    CollectEvents { event ->
      when (event) {
        DiscountsEvent.Retry -> loadIteration++
        is DiscountsEvent.OnSubmitDiscountCode -> addedDiscount = event.code
        DiscountsEvent.DismissBottomSheet -> {
          addedDiscount = null
          paymentUiState = paymentUiState.copy(
            showAddDiscountBottomSheet = false,
            discountError = null,
          )
        }

        DiscountsEvent.ShowBottomSheet -> {
          paymentUiState = paymentUiState.copy(
            showAddDiscountBottomSheet = true,
          )
        }
      }
    }

    LaunchedEffect(loadIteration) {
      paymentUiState = if (loadIteration != 0) {
        paymentUiState.copy(isRetrying = true)
      } else {
        paymentUiState.copy(isLoadingPaymentOverView = true)
      }
      getPaymentOverviewDataUseCase.provide().invoke().fold(
        ifLeft = {
          paymentUiState = paymentUiState.copy(
            error = true,
            isLoadingPaymentOverView = false,
            isRetrying = false,
          )
        },
        ifRight = { paymentOverviewData ->
          paymentUiState = paymentUiState.copy(
            paymentOverview = paymentOverviewData.paymentOverview,
            foreverInformation = paymentOverviewData.foreverInformation,
            error = null,
            isLoadingPaymentOverView = false,
            isRetrying = false,
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

internal sealed interface DiscountsEvent {
  data object Retry : DiscountsEvent

  data object DismissBottomSheet : DiscountsEvent

  data object ShowBottomSheet : DiscountsEvent

  data class OnSubmitDiscountCode(val code: String) : DiscountsEvent
}

internal data class DiscountsUiState(
  val foreverInformation: ForeverInformation?,
  val paymentOverview: PaymentOverview? = null,
  val discountError: String? = null,
  val error: Boolean? = null,
  val showAddDiscountBottomSheet: Boolean = false,
  val isLoadingPaymentOverView: Boolean = true,
  val isRetrying: Boolean = false,
  val isAddingDiscount: Boolean = false,
)
