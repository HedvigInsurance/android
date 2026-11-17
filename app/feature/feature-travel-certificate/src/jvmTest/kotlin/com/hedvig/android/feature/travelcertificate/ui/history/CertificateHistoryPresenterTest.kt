package com.hedvig.android.feature.travelcertificate.ui.history

import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.fileupload.DownloadedFile
import com.hedvig.android.data.addons.data.AddonBannerInfo
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.addons.data.FlowType
import com.hedvig.android.feature.travelcertificate.FakeCheckAvailabilityUseCase
import com.hedvig.android.feature.travelcertificate.FakeDownloadPdfUseCase
import com.hedvig.android.feature.travelcertificate.FakeGetAddonBannerInfoUseCase
import com.hedvig.android.feature.travelcertificate.FakeGetEligibleContractsWithAddressUseCase
import com.hedvig.android.feature.travelcertificate.FakeGetTravelCertificatesHistoryUseCase
import com.hedvig.android.feature.travelcertificate.data.ContractEligibleWithAddress
import com.hedvig.android.feature.travelcertificate.data.TravelCertificate
import com.hedvig.android.feature.travelcertificate.expectNoStateChange
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class CertificateHistoryPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val history = FakeGetTravelCertificatesHistoryUseCase()
  private val availability = FakeCheckAvailabilityUseCase()
  private val eligibleContracts = FakeGetEligibleContractsWithAddressUseCase()
  private val addonBanners = FakeGetAddonBannerInfoUseCase()
  private val downloadPdf = FakeDownloadPdfUseCase()

  private val presenter = CertificateHistoryPresenter(
    history,
    downloadPdf,
    eligibleContracts,
    availability,
    addonBanners,
  )

  private val activeCertificate = TravelCertificate(
    startDate = LocalDate(2026, 11, 1),
    id = "active",
    signedUrl = "https://example.com/active.pdf",
    expiryDate = LocalDate(2026, 12, 15),
    isExpiredNow = false,
  )
  private val expiredCertificate = TravelCertificate(
    startDate = LocalDate(2025, 6, 1),
    id = "expired",
    signedUrl = "https://example.com/expired.pdf",
    expiryDate = LocalDate(2025, 7, 16),
    isExpiredNow = true,
  )
  private val home = ContractEligibleWithAddress(address = "Street 1", contractId = "home")
  private val summerHouse = ContractEligibleWithAddress(address = "Lake 2", contractId = "summerHouse")
  private val travelBanner = AddonBannerInfo(
    title = "Travel Plus",
    description = "Longer trips",
    labels = listOf("Popular"),
    eligibleInsurancesIds = nonEmptyListOf("home", "summerHouse"),
    flowType = FlowType.APP_TRAVEL_PLUS_SELL_OR_UPGRADE,
  )
  private val otherBanner = travelBanner.copy(title = "Other", eligibleInsurancesIds = nonEmptyListOf("home"))

  private fun content(
    certificates: List<TravelCertificate> = listOf(activeCertificate, expiredCertificate),
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
    idsToNavigateToAddonPurchase = null,
  )

  private fun respond(
    certificates: List<TravelCertificate> = listOf(activeCertificate, expiredCertificate),
    canCreate: Boolean = true,
    contracts: List<ContractEligibleWithAddress> = listOf(home),
    banners: List<AddonBannerInfo> = emptyList(),
  ) {
    history.responses.trySend(certificates.right())
    availability.responses.trySend(canCreate.right())
    eligibleContracts.responses.trySend(contracts.right())
    addonBanners.banners.tryEmit(banners.right())
  }

  @Test
  fun `content lists active and expired certificates in the order the use case gives`() = runTest {
    presenter.test(CertificateHistoryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.Loading)
      respond()
      assertThat(awaitItem()).isEqualTo(content())
      assertThat(addonBanners.sources).containsExactly(AddonBannerSource.TRAVEL_CERTIFICATES)
    }
  }

  @Test
  fun `content waits for every source, the addon banner flow included`() = runTest {
    presenter.test(CertificateHistoryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.Loading)
      history.responses.trySend(listOf(activeCertificate).right())
      availability.responses.trySend(true.right())
      eligibleContracts.responses.trySend(listOf(home).right())
      testScheduler.advanceUntilIdle()
      expectNoStateChange(CertificateHistoryUiState.Loading)
      addonBanners.banners.tryEmit(emptyList<AddonBannerInfo>().right())
      assertThat(awaitItem()).isEqualTo(content(certificates = listOf(activeCertificate)))
    }
  }

  @Test
  fun `an empty history is content`() = runTest {
    presenter.test(CertificateHistoryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.Loading)
      respond(certificates = emptyList())
      assertThat(awaitItem()).isEqualTo(content(certificates = emptyList()))
    }
  }

  @Test
  fun `a member who is not eligible gets no generate button`() = runTest {
    presenter.test(CertificateHistoryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.Loading)
      respond(canCreate = false)
      assertThat(awaitItem()).isEqualTo(content(showGenerateButton = false))
    }
  }

  @Test
  fun `more than one eligible contract means the contract must be chosen first`() = runTest {
    presenter.test(CertificateHistoryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.Loading)
      respond(contracts = listOf(home, summerHouse))
      assertThat(awaitItem()).isEqualTo(content(hasChooseOption = true))
    }
  }

  @Test
  fun `no eligible contracts while eligible still offers create without choosing`() = runTest {
    presenter.test(CertificateHistoryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.Loading)
      respond(canCreate = true, contracts = emptyList())
      assertThat(awaitItem()).isEqualTo(content(showGenerateButton = true, hasChooseOption = false))
    }
  }

  @Test
  fun `the first addon banner is shown`() = runTest {
    presenter.test(CertificateHistoryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.Loading)
      respond(banners = listOf(travelBanner, otherBanner))
      assertThat(awaitItem()).isEqualTo(content(addonBannerInfo = travelBanner))
    }
  }

  @Test
  fun `a failed addon banner load still shows the history, without a banner`() = runTest {
    presenter.test(CertificateHistoryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.Loading)
      history.responses.trySend(listOf(activeCertificate, expiredCertificate).right())
      availability.responses.trySend(true.right())
      eligibleContracts.responses.trySend(listOf(home).right())
      addonBanners.banners.tryEmit(ErrorMessage("offline").left())
      assertThat(awaitItem()).isEqualTo(content(addonBannerInfo = null))
    }
  }

  @Test
  fun `a later addon banner emission updates the content`() = runTest {
    presenter.test(CertificateHistoryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.Loading)
      respond(banners = emptyList())
      assertThat(awaitItem()).isEqualTo(content(addonBannerInfo = null))
      addonBanners.banners.tryEmit(listOf(travelBanner).right())
      assertThat(awaitItem()).isEqualTo(content(addonBannerInfo = travelBanner))
    }
  }

  @Test
  fun `a failure in any required source is a failure`() = runTest {
    for (failing in listOf("history", "availability", "contracts")) {
      val history = FakeGetTravelCertificatesHistoryUseCase()
      val availability = FakeCheckAvailabilityUseCase()
      val eligibleContracts = FakeGetEligibleContractsWithAddressUseCase()
      val addonBanners = FakeGetAddonBannerInfoUseCase()
      val presenter = CertificateHistoryPresenter(
        history,
        FakeDownloadPdfUseCase(),
        eligibleContracts,
        availability,
        addonBanners,
      )
      presenter.test(CertificateHistoryUiState.Loading) {
        assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.Loading)
        val error = ErrorMessage("offline")
        history.responses.trySend(if (failing == "history") error.left() else emptyList<TravelCertificate>().right())
        availability.responses.trySend(if (failing == "availability") error.left() else true.right())
        eligibleContracts.responses.trySend(
          if (failing == "contracts") error.left() else listOf(home).right(),
        )
        addonBanners.banners.tryEmit(emptyList<AddonBannerInfo>().right())
        assertThat(awaitItem(), failing).isEqualTo(CertificateHistoryUiState.FailureDownloadingHistory)
      }
    }
  }

  @Test
  fun `a failure can be reloaded, and the failure stays on screen until the reload answers`() = runTest {
    presenter.test(CertificateHistoryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.Loading)
      history.responses.trySend(ErrorMessage("offline").left())
      availability.responses.trySend(true.right())
      eligibleContracts.responses.trySend(listOf(home).right())
      addonBanners.banners.tryEmit(emptyList<AddonBannerInfo>().right())
      assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.FailureDownloadingHistory)

      sendEvent(CertificateHistoryEvent.RetryLoadData)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(CertificateHistoryUiState.FailureDownloadingHistory)
      assertThat(history.calls).isEqualTo(2)

      history.responses.trySend(listOf(activeCertificate, expiredCertificate).right())
      availability.responses.trySend(true.right())
      eligibleContracts.responses.trySend(listOf(home).right())
      assertThat(awaitItem()).isEqualTo(content())
      assertThat(addonBanners.sources).containsExactly(
        AddonBannerSource.TRAVEL_CERTIFICATES,
        AddonBannerSource.TRAVEL_CERTIFICATES,
      )
    }
  }

  @Test
  fun `launching the addon purchase flow is navigation state until cleared`() = runTest {
    presenter.test(CertificateHistoryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.Loading)
      respond(banners = listOf(travelBanner))
      assertThat(awaitItem()).isEqualTo(content(addonBannerInfo = travelBanner))

      sendEvent(CertificateHistoryEvent.LaunchAddonPurchaseFlow(listOf("home", "summerHouse")))
      assertThat(awaitItem()).isEqualTo(
        content(addonBannerInfo = travelBanner).copy(idsToNavigateToAddonPurchase = listOf("home", "summerHouse")),
      )

      sendEvent(CertificateHistoryEvent.ClearNavigation)
      assertThat(awaitItem()).isEqualTo(content(addonBannerInfo = travelBanner))
    }
  }

  @Test
  fun `downloading a certificate exposes the file until it is processed`() = runTest {
    presenter.test(CertificateHistoryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.Loading)
      respond()
      assertThat(awaitItem()).isEqualTo(content())

      sendEvent(CertificateHistoryEvent.DownloadCertificate(activeCertificate.signedUrl))
      assertThat(awaitItem()).isEqualTo(content().copy(isLoadingCertificate = true))
      assertThat(downloadPdf.urls).containsExactly(activeCertificate.signedUrl)

      val file = DownloadedFile(path = "/tmp/certificate.pdf", name = "certificate.pdf")
      downloadPdf.responses.trySend(file.right())
      assertThat(awaitItem()).isEqualTo(content().copy(travelCertificateUri = file))

      sendEvent(CertificateHistoryEvent.HaveProcessedCertificateUri)
      assertThat(awaitItem()).isEqualTo(content())
    }
  }

  @Test
  fun `a failed download shows an error until dismissed`() = runTest {
    presenter.test(CertificateHistoryUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(CertificateHistoryUiState.Loading)
      respond()
      assertThat(awaitItem()).isEqualTo(content())

      sendEvent(CertificateHistoryEvent.DownloadCertificate(activeCertificate.signedUrl))
      assertThat(awaitItem()).isEqualTo(content().copy(isLoadingCertificate = true))
      downloadPdf.responses.trySend(ErrorMessage("offline").left())
      assertThat(awaitItem()).isEqualTo(content().copy(showDownloadCertificateError = true))

      sendEvent(CertificateHistoryEvent.DismissDownloadCertificateError)
      assertThat(awaitItem()).isEqualTo(content())
    }
  }
}
