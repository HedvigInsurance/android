package com.hedvig.android.feature.travelcertificate.ios

import arrow.core.nonEmptyListOf
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.fileupload.DownloadedFile
import com.hedvig.android.data.addons.data.AddonBannerInfo
import com.hedvig.android.data.addons.data.FlowType
import com.hedvig.android.feature.travelcertificate.data.ContractEligibleWithAddress
import com.hedvig.android.feature.travelcertificate.data.TravelCertificate
import com.hedvig.android.feature.travelcertificate.ui.choose.ChooseContractUiState
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryUiState
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class TravelCertificateHistoryAndChooseScreenStateTest {
  private val signedUrl = "https://example.com/certificate.pdf?signature=secret"

  private fun history(
    certificates: List<TravelCertificate> = emptyList(),
    showGenerateButton: Boolean = true,
    hasChooseOption: Boolean = false,
    addonBannerInfo: AddonBannerInfo? = null,
  ) = CertificateHistoryUiState.SuccessDownloadingHistory(
    certificateHistoryList = certificates,
    showDownloadCertificateError = false,
    showGenerateButton = showGenerateButton,
    travelCertificateUri = null,
    isLoadingCertificate = false,
    hasChooseOption = hasChooseOption,
    addonBannerInfo = addonBannerInfo,
  )

  @Test
  fun `history loading and failure`() {
    assertThat(CertificateHistoryUiState.Loading.toScreenState()).isEqualTo(TravelCertificateHistoryScreenState.Loading)
    assertThat(CertificateHistoryUiState.FailureDownloadingHistory.toScreenState())
      .isEqualTo(TravelCertificateHistoryScreenState.Failed)
  }

  @Test
  fun `an empty history is content without certificates`() {
    assertThat(history().toScreenState()).isEqualTo(
      TravelCertificateHistoryScreenState.Content(
        certificates = emptyList(),
        canCreate = true,
        mustChooseContract = false,
        addonBanner = null,
      ),
    )
  }

  @Test
  fun `history rows keep the backend order with active and expired rows the choose rule and the banner`() {
    val state = history(
      certificates = listOf(
        TravelCertificate(LocalDate(2026, 11, 1), "active", signedUrl, LocalDate(2026, 12, 15), isExpiredNow = false),
        TravelCertificate(LocalDate(2025, 6, 9), "expired", "https://b", LocalDate(2025, 7, 9), isExpiredNow = true),
      ),
      showGenerateButton = false,
      hasChooseOption = true,
      addonBannerInfo = AddonBannerInfo(
        title = "Travel Plus",
        description = "Extended travel insurance",
        labels = listOf("Popular", "New"),
        eligibleInsurancesIds = nonEmptyListOf("contract-1", "contract-2"),
        flowType = FlowType.APP_TRAVEL_PLUS_SELL_OR_UPGRADE,
      ),
    )

    assertThat(state.toScreenState()).isEqualTo(
      TravelCertificateHistoryScreenState.Content(
        certificates = listOf(
          TravelCertificateItem("active", "2026-11-01", "2026-12-15", isExpired = false, signedUrl = signedUrl),
          TravelCertificateItem("expired", "2025-06-09", "2025-07-09", isExpired = true, signedUrl = "https://b"),
        ),
        canCreate = false,
        mustChooseContract = true,
        addonBanner = TravelAddonBanner(
          title = "Travel Plus",
          bannerDescription = "Extended travel insurance",
          labels = listOf("Popular", "New"),
          eligibleContractIds = listOf("contract-1", "contract-2"),
        ),
      ),
    )
  }

  @Test
  fun `the download state of the Android screen does not change the history content`() {
    val downloading = history().copy(
      showDownloadCertificateError = true,
      travelCertificateUri = DownloadedFile("/tmp/certificate.pdf", "application/pdf"),
      isLoadingCertificate = true,
      idsToNavigateToAddonPurchase = listOf("contract-1"),
    )

    assertThat(downloading.toScreenState()).isEqualTo(history().toScreenState())
  }

  @Test
  fun `a history row leaves out its signed url from its text`() {
    val item = TravelCertificateItem("id-1", "2026-11-01", "2026-12-15", isExpired = false, signedUrl = signedUrl)

    assertThat(item.toString())
      .isEqualTo("TravelCertificateItem(id=id-1, startDate=2026-11-01, expiryDate=2026-12-15, isExpired=false)")
  }

  @Test
  fun `history handle calls the closure for each case`() {
    val content = history().toScreenState()
    val calls = listOf(TravelCertificateHistoryScreenState.Loading, TravelCertificateHistoryScreenState.Failed, content)
      .map { state ->
        var called = ""
        state.handle(onLoading = { called = "loading" }, onFailed = { called = "failed" }, onContent = {
          called = "content:${it.certificates.size}"
        })
        called
      }

    assertThat(calls).isEqualTo(listOf("loading", "failed", "content:0"))
  }

  @Test
  fun `choose contract loading failure and contracts in order`() {
    assertThat(ChooseContractUiState.Loading.toScreenState())
      .isEqualTo(TravelCertificateChooseContractScreenState.Loading)
    assertThat(ChooseContractUiState.Failure.toScreenState())
      .isEqualTo(TravelCertificateChooseContractScreenState.Failed)
    assertThat(
      ChooseContractUiState.Success(
        listOf(ContractEligibleWithAddress("Storgatan 1", "contract-1"), ContractEligibleWithAddress("Hem ", "c-2")),
      ).toScreenState(),
    ).isEqualTo(
      TravelCertificateChooseContractScreenState.Content(
        listOf(TravelCertificateContract("contract-1", "Storgatan 1"), TravelCertificateContract("c-2", "Hem ")),
      ),
    )
    assertThat(ChooseContractUiState.Success(emptyList()).toScreenState())
      .isEqualTo(TravelCertificateChooseContractScreenState.Content(emptyList()))
  }

  @Test
  fun `a contract leaves out its address from its text`() {
    assertThat(TravelCertificateContract("contract-1", "Storgatan 1").toString())
      .isEqualTo("TravelCertificateContract(id=contract-1)")
  }

  @Test
  fun `choose contract handle calls the closure for each case`() {
    val calls = listOf(
      TravelCertificateChooseContractScreenState.Loading,
      TravelCertificateChooseContractScreenState.Failed,
      TravelCertificateChooseContractScreenState.Content(listOf(TravelCertificateContract("c", "a"))),
    ).map { state ->
      var called = ""
      state.handle(onLoading = { called = "loading" }, onFailed = { called = "failed" }, onContent = {
        called = "content:${it.contracts.single().id}"
      })
      called
    }

    assertThat(calls).isEqualTo(listOf("loading", "failed", "content:c"))
  }
}
