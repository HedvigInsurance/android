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

internal class TrustlyPayoutPresenter(
  private val trustlyCallback: TrustlyCallback,
  private val startTrustlyPayoutSessionUseCase: StartTrustlyPayoutSessionUseCase,
  private val cacheManager: NetworkCacheManager,
) : MoleculePresenter<TrustlyEvent, TrustlyUiState> {
  @Composable
  override fun MoleculePresenterScope<TrustlyEvent>.present(lastState: TrustlyUiState): TrustlyUiState {
    var browsing: TrustlyUiState.Browsing? by remember {
      mutableStateOf(lastState.safeCast<TrustlyUiState.Browsing>())
    }
    var startSessionError: ErrorMessage? by remember { mutableStateOf(null) }
    var connectingCardFailed by remember { mutableStateOf(lastState is TrustlyUiState.FailedToConnectCard) }
    var succeededInConnectingCard by remember { mutableStateOf(lastState is TrustlyUiState.SucceededInConnectingCard) }

    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      if (browsing != null) return@LaunchedEffect
      if (startSessionError != null) return@LaunchedEffect
      if (connectingCardFailed) return@LaunchedEffect
      if (succeededInConnectingCard) return@LaunchedEffect
      startTrustlyPayoutSessionUseCase.invoke().fold(
        ifLeft = {
          startSessionError = it
          browsing = null
        },
        ifRight = {
          // todo testing remove hardcoded success
          if (it.url.startsWith("fake://")) {
            succeededInConnectingCard = true
          } else {
            startSessionError = null
            browsing = TrustlyUiState.Browsing(it.url, trustlyCallback)
          }
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        TrustlyEvent.ConnectingCardFailed -> {
          connectingCardFailed = true
        }

        TrustlyEvent.ConnectingCardSucceeded -> {
          succeededInConnectingCard = true
        }

        TrustlyEvent.RetryConnectingCard -> {
          browsing = null
          startSessionError = null
          connectingCardFailed = false
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
    if (connectingCardFailed) {
      return TrustlyUiState.FailedToConnectCard
    }
    if (startSessionError != null) {
      return TrustlyUiState.FailedToStartSession
    }
    val browsingValue = browsing
    if (browsingValue != null) {
      return browsingValue
    }
    return TrustlyUiState.Loading
  }
}
