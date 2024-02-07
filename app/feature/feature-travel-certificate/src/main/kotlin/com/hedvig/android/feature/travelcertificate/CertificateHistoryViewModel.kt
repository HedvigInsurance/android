package com.hedvig.android.feature.travelcertificate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
) : MoleculeViewModel<CertificateHistoryEvent, CertificateHistoryUiState>(
    initialState = CertificateHistoryUiState.Loading,
    presenter = CertificateHistoryPresenter(getTravelCertificatesHistoryUseCase, downloadTravelCertificateUseCase),
  )

internal class CertificateHistoryPresenter(
  private val getTravelCertificatesHistoryUseCase: GetTravelCertificatesHistoryUseCase,
  private val downloadTravelCertificateUseCase: DownloadTravelCertificateUseCase,
) :
  MoleculePresenter<CertificateHistoryEvent, CertificateHistoryUiState> {
  @Composable
  override fun MoleculePresenterScope<CertificateHistoryEvent>.present(
    lastState: CertificateHistoryUiState,
  ): CertificateHistoryUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var dataLoadIteration by remember { mutableIntStateOf(0) }

    val noUrl = null
    var downloadingUrl by remember {
      mutableStateOf<String?>(noUrl)
    }

    val noUri = null
    var downloadingUri by remember {
      mutableStateOf<TravelCertificateUri?>(noUri)
    }

    CollectEvents { event ->
      when (event) {
        CertificateHistoryEvent.RetryLoadReferralData -> dataLoadIteration++
        is CertificateHistoryEvent.DownloadCertificate -> downloadingUrl = event.signedUrl
      }
    }

    LaunchedEffect(downloadingUrl) {
      if (downloadingUrl != noUrl) {
        currentState = CertificateHistoryUiState.Loading
        logcat(LogPriority.INFO) { "Downloading travel certificate with url:$downloadingUrl" }
        downloadingUrl?.let {
          downloadTravelCertificateUseCase.invoke(TravelCertificateUrl(it))
            .fold(
              ifLeft = { errorMessage ->
                logcat(LogPriority.ERROR) { "Downloading travel certificate failed:$errorMessage" }
                currentState = CertificateHistoryUiState.FailureDownloadingCertificate
                dataLoadIteration++
                downloadingUrl = noUrl
              },
              ifRight = { uri ->
                logcat(
                  LogPriority.INFO,
                ) { "Downloading travel certificate succeeded. Result uri:${uri.uri.absolutePath}" }
                downloadingUri = uri
                downloadingUrl = noUrl
                currentState = CertificateHistoryUiState.SuccessDownloadingCertificate(uri)
                dataLoadIteration++
              },
            )
        } ?: {
          logcat(LogPriority.ERROR) { "Downloading travel certificate failed: url is null" }
          currentState = CertificateHistoryUiState.FailureDownloadingCertificate
          downloadingUrl = noUrl
          dataLoadIteration++
        }
      }
    }

    LaunchedEffect(dataLoadIteration) {
      currentState = CertificateHistoryUiState.Loading
      getTravelCertificatesHistoryUseCase.invoke()
        .onLeft {
          logcat { "Could not fetch travel certificates history. Message: ${it.message}" }
          currentState = CertificateHistoryUiState.FailureDownloadingHistory
        }
        .onRight {
          logcat(LogPriority.INFO) { "Successfully fetched travel certificates history." }
          currentState = CertificateHistoryUiState.SuccessDownloadingHistory(it)
        }
    }
    return currentState
  }
}

sealed interface CertificateHistoryEvent {
  data object RetryLoadReferralData : CertificateHistoryEvent

  data class DownloadCertificate(val signedUrl: String) : CertificateHistoryEvent
}

internal sealed interface CertificateHistoryUiState {
  data class SuccessDownloadingHistory(
    val certificateHistoryList: List<TravelCertificate>,
  ) : CertificateHistoryUiState

  data class SuccessDownloadingCertificate(
    val downLoadingUri: TravelCertificateUri?,
  ) : CertificateHistoryUiState

  data object FailureDownloadingCertificate : CertificateHistoryUiState

  data object FailureDownloadingHistory : CertificateHistoryUiState

  data object Loading : CertificateHistoryUiState
}
