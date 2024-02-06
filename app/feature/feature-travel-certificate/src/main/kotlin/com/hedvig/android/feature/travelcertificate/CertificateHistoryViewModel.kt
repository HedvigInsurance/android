package com.hedvig.android.feature.travelcertificate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.travelcertificate.GetTravelCertificatesHistoryUseCase
import com.hedvig.android.data.travelcertificate.TravelCertificate
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class CertificateHistoryViewModel(
  getTravelCertificatesHistoryUseCase: GetTravelCertificatesHistoryUseCase,
) : MoleculeViewModel<CertificateHistoryEvent, CertificateHistoryUiState>(
    initialState = CertificateHistoryUiState.Loading,
    presenter = CertificateHistoryPresenter(getTravelCertificatesHistoryUseCase),
  )

internal class CertificateHistoryPresenter(
  private val getTravelCertificatesHistoryUseCase: GetTravelCertificatesHistoryUseCase,
) :
  MoleculePresenter<CertificateHistoryEvent, CertificateHistoryUiState> {
  @Composable
  override fun MoleculePresenterScope<CertificateHistoryEvent>.present(
    lastState: CertificateHistoryUiState,
  ): CertificateHistoryUiState {
    var isLoading by remember { mutableStateOf(lastState.isLoading) }
    var dataLoadIteration by remember { mutableIntStateOf(0) }
    var certificateHistoryErrorMessage by remember { mutableStateOf(lastState.certificateHistoryErrorMessage) }
    var certificateHistoryList by remember { mutableStateOf(lastState.certificateHistoryList) }
    var showBottomSheet by remember { mutableStateOf(false) }

    CollectEvents { event ->
      when (event) {
        CertificateHistoryEvent.RetryLoadReferralData -> dataLoadIteration++
        CertificateHistoryEvent.OnErrorDialogDismissed -> {
          certificateHistoryErrorMessage = null
        }

        CertificateHistoryEvent.DismissBottomSheet -> showBottomSheet = false
        CertificateHistoryEvent.ShowBottomSheet -> showBottomSheet = true
      }
    }

    LaunchedEffect(dataLoadIteration) {
      logcat { "CertificateHistoryPresenter is fetching again" }
      isLoading = true
      certificateHistoryErrorMessage = null
      either<ErrorMessage, List<TravelCertificate>> {
        val historyData = getTravelCertificatesHistoryUseCase.invoke()
        historyData
      }.fold(
        ifLeft = {
          certificateHistoryErrorMessage = it
        },
        ifRight = {
          certificateHistoryList = it
        },
      )
      isLoading = false
    }

    return CertificateHistoryUiState(
      certificateHistoryList,
      certificateHistoryErrorMessage,
      isLoading,
      showBottomSheet,
    )
  }
}

sealed interface CertificateHistoryEvent {
  data object RetryLoadReferralData : CertificateHistoryEvent

  data object OnErrorDialogDismissed : CertificateHistoryEvent

  data object DismissBottomSheet : CertificateHistoryEvent

  data object ShowBottomSheet : CertificateHistoryEvent
}

internal data class CertificateHistoryUiState(
  val certificateHistoryList: List<TravelCertificate>?,
  val certificateHistoryErrorMessage: ErrorMessage?,
  val isLoading: Boolean,
  val showInfoBottomSheet: Boolean,
) {
  companion object {
    val Loading: CertificateHistoryUiState = CertificateHistoryUiState(
      certificateHistoryList = null,
      certificateHistoryErrorMessage = null,
      isLoading = true,
      showInfoBottomSheet = false,
    )
  }
}
