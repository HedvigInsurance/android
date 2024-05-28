package com.hedvig.android.feature.payments.discounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.payments.data.Discount
import com.hedvig.android.feature.payments.data.GetDiscountsOverviewUseCase
import com.hedvig.android.feature.payments.overview.data.AddDiscountUseCase
import com.hedvig.android.feature.payments.overview.data.ForeverInformation
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class DiscountsPresenter(
  val getDiscountsOverviewUseCase: GetDiscountsOverviewUseCase,
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
            discounts = discountsOverview.discounts,
            foreverInformation = discountsOverview.foreverInformation,
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
  val discounts: List<Discount>,
  val discountError: String? = null,
  val error: Boolean? = null,
  val showAddDiscountBottomSheet: Boolean = false,
  val isLoadingPaymentOverView: Boolean = true,
  val isRetrying: Boolean = false,
  val isAddingDiscount: Boolean = false,
)
