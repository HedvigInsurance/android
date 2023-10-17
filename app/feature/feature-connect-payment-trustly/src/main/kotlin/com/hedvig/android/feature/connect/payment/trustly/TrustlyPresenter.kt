package com.hedvig.android.feature.connect.payment.trustly

import androidx.compose.runtime.Composable
import com.hedvig.android.feature.connect.payment.trustly.data.TrustlyCallback
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class TrustlyPresenter(
  trustlyCallback: TrustlyCallback,
  startTrustlySessionUseCase: StartTrustlySessionUseCase,
) : MoleculePresenter<TrustlyEvent, TrustlyUiState> {
  @Composable
  override fun MoleculePresenterScope<TrustlyEvent>.present(lastState: TrustlyUiState): TrustlyUiState {
    return TrustlyUiState.Loading
  }
}

internal sealed interface TrustlyEvent {
  data object ConnectingCardSucceeded : TrustlyEvent
  data object ConnectingCardFailed : TrustlyEvent
  data object RetryConnectingCard : TrustlyEvent
}

internal interface TrustlyUiState {
  data object Loading : TrustlyUiState

  data class Browsing(
    val url: String,
    val trustlyCallback: TrustlyCallback,
  ) : TrustlyUiState

  data object FailedToConnectCard : TrustlyUiState
  data object SucceededInConnectingCard : TrustlyUiState
}
