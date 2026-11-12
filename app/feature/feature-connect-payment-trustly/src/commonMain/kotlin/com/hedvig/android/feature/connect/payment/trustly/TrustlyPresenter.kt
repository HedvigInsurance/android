package com.hedvig.android.feature.connect.payment.trustly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.feature.connect.payment.trustly.data.TrustlyCallback
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class TrustlyPresenter(
  private val trustlyCallback: TrustlyCallback,
  private val startTrustlySessionUseCase: StartTrustlySessionUseCase,
  private val cacheManager: NetworkCacheManager,
) : MoleculePresenter<TrustlyEvent, TrustlyUiState> {
  @Composable
  override fun MoleculePresenterScope<TrustlyEvent>.present(lastState: TrustlyUiState): TrustlyUiState {
    var browsing: TrustlyUiState.Browsing? by remember {
      mutableStateOf(lastState.safeCast<TrustlyUiState.Browsing>())
    }
    var startSessionError: ErrorMessage? by remember { mutableStateOf(null) }
    var connectingCardFailed by remember { mutableStateOf(lastState is TrustlyUiState.FailedToConnectCard) }
    var cancelledConnectingCard by remember { mutableStateOf(lastState is TrustlyUiState.CancelledConnectingCard) }
    var succeededInConnectingCard by remember { mutableStateOf(lastState is TrustlyUiState.SucceededInConnectingCard) }

    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      if (browsing != null) return@LaunchedEffect
      if (startSessionError != null) return@LaunchedEffect
      if (connectingCardFailed) return@LaunchedEffect
      if (cancelledConnectingCard) return@LaunchedEffect
      if (succeededInConnectingCard) return@LaunchedEffect
      startTrustlySessionUseCase.invoke().fold(
        ifLeft = {
          startSessionError = it
          browsing = null
        },
        ifRight = {
          startSessionError = null
          browsing = TrustlyUiState.Browsing(it.url, trustlyCallback)
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        TrustlyEvent.ConnectingCardFailed -> {
          connectingCardFailed = true
        }

        TrustlyEvent.ConnectingCardCancelled -> {
          cancelledConnectingCard = true
        }

        TrustlyEvent.ConnectingCardSucceeded -> {
          succeededInConnectingCard = true
        }

        TrustlyEvent.RetryConnectingCard -> {
          browsing = null
          startSessionError = null
          connectingCardFailed = false
          cancelledConnectingCard = false
          succeededInConnectingCard = false
          loadIteration++
        }
      }
    }

    if (succeededInConnectingCard) {
      LaunchedEffect(Unit) {
        cacheManager.clearCache()
      }
      return TrustlyUiState.SucceededInConnectingCard
    }
    if (cancelledConnectingCard) {
      return TrustlyUiState.CancelledConnectingCard
    }
    if (connectingCardFailed) {
      return TrustlyUiState.FailedToConnectCard
    }
    val startSessionErrorValue = startSessionError
    if (startSessionErrorValue != null) {
      return TrustlyUiState.FailedToStartSession(startSessionErrorValue)
    }
    val browsingValue = browsing
    if (browsingValue != null) {
      return browsingValue
    }
    return TrustlyUiState.Loading
  }
}

internal sealed interface TrustlyEvent {
  data object ConnectingCardSucceeded : TrustlyEvent

  data object ConnectingCardFailed : TrustlyEvent

  /** The member ended the Trustly checkout themselves. */
  data object ConnectingCardCancelled : TrustlyEvent

  data object RetryConnectingCard : TrustlyEvent
}

internal sealed interface TrustlyUiState {
  data object Loading : TrustlyUiState

  data class Browsing(
    val url: String,
    val trustlyCallback: TrustlyCallback,
  ) : TrustlyUiState

  data object FailedToConnectCard : TrustlyUiState

  /** Terminal state for a checkout the member ended themselves, which offers no retry. */
  data object CancelledConnectingCard : TrustlyUiState

  data class FailedToStartSession(val errorMessage: ErrorMessage) : TrustlyUiState

  data object SucceededInConnectingCard : TrustlyUiState
}
