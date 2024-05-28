package com.hedvig.android.feature.connect.payment.adyen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.feature.connect.payment.adyen.data.AdyenPaymentUrl
import com.hedvig.android.feature.connect.payment.adyen.data.GetAdyenPaymentUrlUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class AdyenPresenter(
  private val getAdyenPaymentUrlUseCase: GetAdyenPaymentUrlUseCase,
) : MoleculePresenter<AdyenEvent, AdyenUiState> {
  @Composable
  override fun MoleculePresenterScope<AdyenEvent>.present(lastState: AdyenUiState): AdyenUiState {
    var browsing: AdyenUiState.Browsing? by remember {
      mutableStateOf(lastState.safeCast<AdyenUiState.Browsing>())
    }

    var getPaymentLinkError: ErrorMessage? by remember { mutableStateOf(null) }
    var connectingCardFailed by remember { mutableStateOf(lastState is AdyenUiState.FailedToConnectCard) }
    var succeededInConnectingCard by remember { mutableStateOf(lastState is AdyenUiState.SucceededInConnectingCard) }

    var loadIteration by remember { mutableIntStateOf(0) }
    LaunchedEffect(loadIteration) {
      if (browsing != null) return@LaunchedEffect
      if (getPaymentLinkError != null) return@LaunchedEffect
      if (connectingCardFailed) return@LaunchedEffect
      if (succeededInConnectingCard) return@LaunchedEffect
      getAdyenPaymentUrlUseCase.invoke().fold(
        ifLeft = {
          getPaymentLinkError = it
          browsing = null
        },
        ifRight = { adyenPaymentUrl ->
          getPaymentLinkError = null
          browsing = AdyenUiState.Browsing(adyenPaymentUrl)
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        AdyenEvent.ConnectingCardFailed -> {
          connectingCardFailed = true
        }
        AdyenEvent.ConnectingCardSucceeded -> {
          succeededInConnectingCard = true
        }
        AdyenEvent.RetryLoadingPaymentLink -> {
          browsing = null
          getPaymentLinkError = null
          connectingCardFailed = false
          succeededInConnectingCard = false
          loadIteration++
        }
      }
    }

    if (succeededInConnectingCard) {
      return AdyenUiState.SucceededInConnectingCard
    }
    if (connectingCardFailed) {
      return AdyenUiState.FailedToConnectCard
    }
    if (getPaymentLinkError != null) {
      return AdyenUiState.FailedToGetPaymentLink
    }
    val browsingValue = browsing
    if (browsingValue != null) {
      return browsingValue
    }
    return AdyenUiState.Loading
  }
}

internal sealed interface AdyenEvent {
  data object RetryLoadingPaymentLink : AdyenEvent

  data object ConnectingCardFailed : AdyenEvent

  data object ConnectingCardSucceeded : AdyenEvent
}

internal interface AdyenUiState {
  data object Loading : AdyenUiState

  data class Browsing(
    val adyenPaymentUrl: AdyenPaymentUrl,
  ) : AdyenUiState

  data object FailedToConnectCard : AdyenUiState

  data object FailedToGetPaymentLink : AdyenUiState

  data object SucceededInConnectingCard : AdyenUiState
}
