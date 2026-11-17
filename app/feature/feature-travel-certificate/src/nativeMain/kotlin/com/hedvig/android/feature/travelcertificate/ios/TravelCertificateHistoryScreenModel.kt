@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.feature.travelcertificate.ios

import androidx.lifecycle.ViewModelProvider
import com.hedvig.android.data.addons.data.AddonBannerInfo
import com.hedvig.android.feature.travelcertificate.data.TravelCertificate
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryEvent
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryUiState
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryViewModel
import com.hedvig.android.molecule.public.ScreenModel
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
fun createTravelCertificateHistoryScreenModel(
  viewModelFactory: ViewModelProvider.Factory,
): TravelCertificateHistoryScreenModel = TravelCertificateHistoryScreenModel(viewModelFactory)

/**
 * The member's travel certificates. The Swift owner's rules are on [ScreenModel], and one more applies here: the list,
 * the eligibility and the add-on offer are loaded once per model, so call [reload] when a create flow started from
 * this screen ends, however it ends (Created, dismissed, or closed while creating).
 */
class TravelCertificateHistoryScreenModel internal constructor(viewModelFactory: ViewModelProvider.Factory) :
  ScreenModel<TravelCertificateHistoryScreenState>() {
    private val viewModel = viewModel(viewModelFactory, CertificateHistoryViewModel::class) { it.toScreenState() }

    /**
     * Loads the list, the eligibility and the add-on offer again, keeping the shown state until they arrive. Safe to
     * call from any thread.
     */
    fun reload() {
      viewModel.send(CertificateHistoryEvent.RetryLoadData)
    }
  }

internal fun CertificateHistoryUiState.toScreenState(): TravelCertificateHistoryScreenState = when (this) {
  CertificateHistoryUiState.Loading -> TravelCertificateHistoryScreenState.Loading

  CertificateHistoryUiState.FailureDownloadingHistory -> TravelCertificateHistoryScreenState.Failed

  is CertificateHistoryUiState.SuccessDownloadingHistory -> TravelCertificateHistoryScreenState.Content(
    certificates = certificateHistoryList.map { it.toItem() },
    canCreate = showGenerateButton,
    mustChooseContract = hasChooseOption,
    addonBanner = addonBannerInfo?.toBanner(),
  )
}

private fun TravelCertificate.toItem(): TravelCertificateItem = TravelCertificateItem(
  id = id,
  startDate = startDate.toIsoDateString(),
  expiryDate = expiryDate.toIsoDateString(),
  isExpired = isExpiredNow,
  signedUrl = signedUrl,
)

private fun AddonBannerInfo.toBanner(): TravelAddonBanner = TravelAddonBanner(
  title = title,
  bannerDescription = description,
  labels = labels,
  eligibleContractIds = eligibleInsurancesIds.toList(),
)
