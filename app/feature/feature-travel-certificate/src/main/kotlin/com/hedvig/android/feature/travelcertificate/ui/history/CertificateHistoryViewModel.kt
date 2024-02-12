package com.hedvig.android.feature.travelcertificate.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.fx.coroutines.parZip
import com.hedvig.android.data.travelcertificate.CheckTravelCertificateAvailabilityForCurrentContractsUseCase
import com.hedvig.android.data.travelcertificate.GetTravelCertificatesHistoryUseCase
import com.hedvig.android.data.travelcertificate.TravelCertificate
import com.hedvig.android.feature.travelcertificate.data.DownloadTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUri
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class CertificateHistoryViewModel(
  getTravelCertificatesHistoryUseCase: GetTravelCertificatesHistoryUseCase,
  downloadTravelCertificateUseCase: DownloadTravelCertificateUseCase,
  checkTravelCertificateAvailabilityForCurrentContractsUseCase: CheckTravelCertificateAvailabilityForCurrentContractsUseCase,
) : MoleculeViewModel<CertificateHistoryEvent, CertificateHistoryUiState>(
    initialState = CertificateHistoryUiState.Loading,
    presenter = CertificateHistoryPresenter(
      getTravelCertificatesHistoryUseCase,
      downloadTravelCertificateUseCase,
      checkTravelCertificateAvailabilityForCurrentContractsUseCase,
    ),
  )

internal class CertificateHistoryPresenter(
  private val getTravelCertificatesHistoryUseCase: GetTravelCertificatesHistoryUseCase,
  private val downloadTravelCertificateUseCase: DownloadTravelCertificateUseCase,
  private val checkTravelCertificateAvailabilityForCurrentContractsUseCase: CheckTravelCertificateAvailabilityForCurrentContractsUseCase,
) :
  MoleculePresenter<CertificateHistoryEvent, CertificateHistoryUiState> {
  @Composable
  override fun MoleculePresenterScope<CertificateHistoryEvent>.present(
    lastState: CertificateHistoryUiState,
  ): CertificateHistoryUiState {
    var dataLoadIteration by remember { mutableIntStateOf(0) }

    var downloadingUrl by remember {
      mutableStateOf<String?>(null)
    }

    var savedFileUri by remember {
      mutableStateOf<TravelCertificateUri?>(null)
    }

    var screenContentState by remember {
      mutableStateOf<ScreenContentState>(ScreenContentState.Loading)
    }
    var showErrorDialog by remember {
      mutableStateOf(false)
    }

    var isLoadingCertificate by remember {
      mutableStateOf(false)
    }

    CollectEvents { event ->
      when (event) {
        CertificateHistoryEvent.RetryLoadReferralData -> dataLoadIteration++
        is CertificateHistoryEvent.DownloadCertificate -> downloadingUrl = event.signedUrl
        CertificateHistoryEvent.DismissDownloadCertificateError -> {
          showErrorDialog = false
        }

        CertificateHistoryEvent.HaveProcessedCertificateUri -> {
          savedFileUri = null
        }
      }
    }

    LaunchedEffect(downloadingUrl) {
      isLoadingCertificate = false
      val downloadingUrlValue = downloadingUrl ?: return@LaunchedEffect
      isLoadingCertificate = true
      logcat(LogPriority.INFO) { "Downloading travel certificate with url:$downloadingUrl" }
      downloadTravelCertificateUseCase.invoke(TravelCertificateUrl(downloadingUrlValue))
        .fold(
          ifLeft = { errorMessage ->
            isLoadingCertificate = false
            logcat(LogPriority.ERROR) { "Downloading travel certificate failed:$errorMessage" }
            showErrorDialog = true
            downloadingUrl = null
          },
          ifRight = { uri ->
            isLoadingCertificate = false
            logcat(
              LogPriority.INFO,
            ) { "Downloading travel certificate succeeded. Result uri:${uri.uri.absolutePath}" }
            savedFileUri = uri
            downloadingUrl = null
          },
        )
    }

    LaunchedEffect(dataLoadIteration) {
      parZip(
        { getTravelCertificatesHistoryUseCase.invoke() },
        { checkTravelCertificateAvailabilityForCurrentContractsUseCase.invoke() },
      ) { travelCertificateHistoryResult, eligibilityResult ->
        val history = travelCertificateHistoryResult.getOrNull()
        val eligibility = eligibilityResult.getOrNull()
        screenContentState = if (history != null && eligibility != null) {
          logcat(LogPriority.INFO) { "Successfully fetched travel certificates history." }
          ScreenContentState.Success(history, eligibility)
        } else {
          logcat { "Could not fetch travel certificates history and eligibility" }
          ScreenContentState.Failed
        }
      }
    }
    return when (val screenContentStateValue = screenContentState) {
      ScreenContentState.Failed -> CertificateHistoryUiState.FailureDownloadingHistory
      ScreenContentState.Loading -> CertificateHistoryUiState.Loading
      is ScreenContentState.Success -> CertificateHistoryUiState.SuccessDownloadingHistory(
        screenContentStateValue.certificateHistoryList,
        showErrorDialog,
        screenContentStateValue.eligibleToCreateCertificate,
        savedFileUri,
        isLoadingCertificate,
      )
    }
  }
}

private sealed interface ScreenContentState {
  data object Failed : ScreenContentState

  data object Loading : ScreenContentState

  data class Success(val certificateHistoryList: List<TravelCertificate>, val eligibleToCreateCertificate: Boolean) :
    ScreenContentState
}

sealed interface CertificateHistoryEvent {
  data object RetryLoadReferralData : CertificateHistoryEvent

  data class DownloadCertificate(val signedUrl: String) : CertificateHistoryEvent

  data object HaveProcessedCertificateUri : CertificateHistoryEvent

  data object DismissDownloadCertificateError : CertificateHistoryEvent
}

internal sealed interface CertificateHistoryUiState {
  data class SuccessDownloadingHistory(
    val certificateHistoryList: List<TravelCertificate>,
    val showDownloadCertificateError: Boolean,
    val showGenerateButton: Boolean,
    val travelCertificateUri: TravelCertificateUri?,
    val isLoadingCertificate: Boolean,
  ) : CertificateHistoryUiState

  data object FailureDownloadingHistory : CertificateHistoryUiState

  data object Loading : CertificateHistoryUiState
}
