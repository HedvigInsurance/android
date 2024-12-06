package com.hedvig.android.feature.travelcertificate.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.fx.coroutines.parZip
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.feature.travelcertificate.data.CheckTravelCertificateAvailabilityForCurrentContractsUseCase
import com.hedvig.android.feature.travelcertificate.data.GetEligibleContractsWithAddressUseCase
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificatesHistoryUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificate
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import java.io.File

internal class CertificateHistoryViewModel(
  getTravelCertificatesHistoryUseCase: GetTravelCertificatesHistoryUseCase,
  downloadPdfUseCase: DownloadPdfUseCase,
  checkTravelCertificateAvailabilityForCurrentContractsUseCase:
    CheckTravelCertificateAvailabilityForCurrentContractsUseCase,
  getEligibleContractsWithAddressUseCase: GetEligibleContractsWithAddressUseCase,
  getTravelAddonBannerInfoUseCase: com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCase
) : MoleculeViewModel<CertificateHistoryEvent, CertificateHistoryUiState>(
    initialState = CertificateHistoryUiState.Loading,
    presenter = CertificateHistoryPresenter(
      getTravelCertificatesHistoryUseCase,
      downloadPdfUseCase,
      getEligibleContractsWithAddressUseCase,
      checkTravelCertificateAvailabilityForCurrentContractsUseCase,
      getTravelAddonBannerInfoUseCase
    ),
  )

internal class CertificateHistoryPresenter(
  private val getTravelCertificatesHistoryUseCase: GetTravelCertificatesHistoryUseCase,
  private val downloadPdfUseCase: DownloadPdfUseCase,
  private val getEligibleContractsWithAddressUseCase: GetEligibleContractsWithAddressUseCase,
  private val checkTravelCertificateAvailabilityForCurrentContractsUseCase:
    CheckTravelCertificateAvailabilityForCurrentContractsUseCase,
  private val getTravelAddonBannerInfoUseCase: com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCase
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
      mutableStateOf<File?>(null)
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
        CertificateHistoryEvent.RetryLoadData -> dataLoadIteration++
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
      downloadPdfUseCase.invoke(downloadingUrlValue)
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
            ) { "Downloading travel certificate succeeded. Result uri:${uri.absolutePath}" }
            savedFileUri = uri
            downloadingUrl = null
          },
        )
    }

    LaunchedEffect(dataLoadIteration) {
      parZip(
        { getTravelCertificatesHistoryUseCase.invoke() },
        { checkTravelCertificateAvailabilityForCurrentContractsUseCase.invoke() },
        { getEligibleContractsWithAddressUseCase.invoke() },
        { com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCase.invoke() }
      ) { travelCertificateHistoryResult, eligibilityResult, eligibleContractsResult, travelAddonBannerResult ->
        val history = travelCertificateHistoryResult.getOrNull()
        val eligibility = eligibilityResult.getOrNull()
        val eligibleContracts = eligibleContractsResult.getOrNull()
        val travelAddonBanner = travelAddonBannerResult.getOrNull()
        screenContentState = if (history != null && eligibility != null && eligibleContracts != null) {
          val hasChooseOption = eligibleContracts.size > 1
          logcat(LogPriority.INFO) { "Successfully fetched travel certificates history." }
          ScreenContentState.Success(
            history,
            eligibility,
            hasChooseOption,
            travelAddonBannerInfo = travelAddonBanner)
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
        screenContentStateValue.mustChooseContractBeforeGeneratingTravelCertificate,
        screenContentStateValue.travelAddonBannerInfo
      )
    }
  }
}

private sealed interface ScreenContentState {
  data object Failed : ScreenContentState

  data object Loading : ScreenContentState

  data class Success(
    val certificateHistoryList: List<TravelCertificate>,
    val eligibleToCreateCertificate: Boolean,
    val mustChooseContractBeforeGeneratingTravelCertificate: Boolean,
    val travelAddonBannerInfo: com.hedvig.android.data.addons.data.TravelAddonBannerInfo?
  ) :
    ScreenContentState
}

sealed interface CertificateHistoryEvent {
  data object RetryLoadData : CertificateHistoryEvent

  data class DownloadCertificate(val signedUrl: String) : CertificateHistoryEvent

  data object HaveProcessedCertificateUri : CertificateHistoryEvent

  data object DismissDownloadCertificateError : CertificateHistoryEvent
}

internal sealed interface CertificateHistoryUiState {
  data class SuccessDownloadingHistory(
    val certificateHistoryList: List<TravelCertificate>,
    val showDownloadCertificateError: Boolean,
    val showGenerateButton: Boolean,
    val travelCertificateUri: File?,
    val isLoadingCertificate: Boolean,
    val hasChooseOption: Boolean,
    val travelAddonBannerInfo: com.hedvig.android.data.addons.data.TravelAddonBannerInfo?
  ) : CertificateHistoryUiState

  data object FailureDownloadingHistory : CertificateHistoryUiState

  data object Loading : CertificateHistoryUiState
}
