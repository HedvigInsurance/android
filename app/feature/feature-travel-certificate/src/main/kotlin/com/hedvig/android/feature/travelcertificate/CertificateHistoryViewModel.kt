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
//    val downloadingErrorString = stringResource(id = R.string.travel_certificate_downloading_error)
    // todo: doesn't work like this, get runtime error: java.lang.IllegalStateException: CompositionLocal LocalConfiguration not present
    val downloadingErrorString = "downloading error"
    var isLoading by remember { mutableStateOf(lastState.isLoading) }
    var dataLoadIteration by remember { mutableIntStateOf(0) }
    var certificateHistoryErrorMessage by remember { mutableStateOf(lastState.certificateHistoryErrorMessage) }
    var certificateHistoryList by remember { mutableStateOf(lastState.certificateHistoryList) }
    var showBottomSheet by remember { mutableStateOf(false) }
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
        CertificateHistoryEvent.OnErrorDialogDismissed -> {
          certificateHistoryErrorMessage = null
        }

        CertificateHistoryEvent.DismissBottomSheet -> showBottomSheet = false
        CertificateHistoryEvent.ShowBottomSheet -> showBottomSheet = true
        is CertificateHistoryEvent.DownloadCertificate -> downloadingUrl = event.signedUrl
      }
    }

    LaunchedEffect(downloadingUrl) {
      if (downloadingUrl != noUrl) {
        isLoading = true
        logcat(LogPriority.INFO) { "Downloading travel certificate with url:$downloadingUrl" }
        downloadingUrl?.let {
          downloadTravelCertificateUseCase.invoke(TravelCertificateUrl(it))
            .fold(
              ifLeft = { errorMessage ->
                logcat(LogPriority.ERROR) { "Downloading travel certificate failed:$errorMessage" }
                isLoading = false
                certificateHistoryErrorMessage = errorMessage
                downloadingUrl = noUrl
              },
              ifRight = { uri ->
                logcat(
                  LogPriority.INFO,
                ) { "Downloading travel certificate succeeded. Result uri:${uri.uri.absolutePath}" }
                isLoading = false
                downloadingUri = uri
                downloadingUrl = noUrl
              },
            )
        } ?: {
          logcat(LogPriority.ERROR) { "Downloading travel certificate failed: url is null" }
          isLoading = false
          certificateHistoryErrorMessage = ErrorMessage(downloadingErrorString)
          downloadingUrl = noUrl
        }
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
      downloadingUri,
    )
  }
}

sealed interface CertificateHistoryEvent {
  data object RetryLoadReferralData : CertificateHistoryEvent

  data object OnErrorDialogDismissed : CertificateHistoryEvent

  data object DismissBottomSheet : CertificateHistoryEvent

  data object ShowBottomSheet : CertificateHistoryEvent

  data class DownloadCertificate(val signedUrl: String) : CertificateHistoryEvent
}

internal data class CertificateHistoryUiState(
  val certificateHistoryList: List<TravelCertificate>?,
  val certificateHistoryErrorMessage: ErrorMessage?,
  val isLoading: Boolean,
  val showInfoBottomSheet: Boolean,
  val downLoadingUri: TravelCertificateUri?,
) {
  companion object {
    val Loading: CertificateHistoryUiState = CertificateHistoryUiState(
      certificateHistoryList = null,
      certificateHistoryErrorMessage = null,
      isLoading = true,
      showInfoBottomSheet = false,
      downLoadingUri = null,
    )
  }
}
