package com.hedvig.android.feature.purchase.apartment.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.apartment.data.AddToCartAndStartSignUseCase
import com.hedvig.android.feature.purchase.apartment.navigation.SigningParameters
import com.hedvig.android.feature.purchase.apartment.navigation.SummaryParameters
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class PurchaseSummaryViewModel(
  summaryParameters: SummaryParameters,
  addToCartAndStartSignUseCase: AddToCartAndStartSignUseCase,
) : MoleculeViewModel<PurchaseSummaryEvent, PurchaseSummaryUiState>(
    initialState = PurchaseSummaryUiState(
      params = summaryParameters,
      isSubmitting = false,
      signingToNavigate = null,
      navigateToFailure = false,
    ),
    presenter = PurchaseSummaryPresenter(
      summaryParameters,
      addToCartAndStartSignUseCase,
    ),
  )

internal class PurchaseSummaryPresenter(
  private val summaryParameters: SummaryParameters,
  private val addToCartAndStartSignUseCase: AddToCartAndStartSignUseCase,
) : MoleculePresenter<PurchaseSummaryEvent, PurchaseSummaryUiState> {
  @Composable
  override fun MoleculePresenterScope<PurchaseSummaryEvent>.present(
    lastState: PurchaseSummaryUiState,
  ): PurchaseSummaryUiState {
    var confirmIteration by remember { mutableIntStateOf(0) }
    var isSubmitting by remember { mutableStateOf(lastState.isSubmitting) }
    var signingToNavigate by remember { mutableStateOf(lastState.signingToNavigate) }
    var navigateToFailure by remember { mutableStateOf(lastState.navigateToFailure) }

    CollectEvents { event ->
      when (event) {
        PurchaseSummaryEvent.Confirm -> {
          confirmIteration++
        }

        PurchaseSummaryEvent.ClearNavigation -> {
          signingToNavigate = null
          navigateToFailure = false
        }
      }
    }

    LaunchedEffect(confirmIteration) {
      if (confirmIteration > 0) {
        isSubmitting = true
        addToCartAndStartSignUseCase.invoke(
          summaryParameters.shopSessionId,
          summaryParameters.selectedOffer.offerId,
        ).fold(
          ifLeft = {
            isSubmitting = false
            navigateToFailure = true
          },
          ifRight = { signingStart ->
            isSubmitting = false
            signingToNavigate = SigningParameters(
              signingId = signingStart.signingId,
              autoStartToken = signingStart.autoStartToken,
              startDate = null,
            )
          },
        )
      }
    }

    return PurchaseSummaryUiState(
      params = summaryParameters,
      isSubmitting = isSubmitting,
      signingToNavigate = signingToNavigate,
      navigateToFailure = navigateToFailure,
    )
  }
}

internal data class PurchaseSummaryUiState(
  val params: SummaryParameters,
  val isSubmitting: Boolean,
  val signingToNavigate: SigningParameters?,
  val navigateToFailure: Boolean,
)

internal sealed interface PurchaseSummaryEvent {
  data object Confirm : PurchaseSummaryEvent

  data object ClearNavigation : PurchaseSummaryEvent
}
