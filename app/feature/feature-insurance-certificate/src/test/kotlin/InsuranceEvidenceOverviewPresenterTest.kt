package com.hedvig.android.feature.insurance.certificate.ui.email

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewEvent
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewPresenter
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewState
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

internal class InsuranceEvidenceOverviewPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private fun createPresenterWithFakes(): Pair<InsuranceEvidenceOverviewPresenter, FakeDownloadPdfUseCase> {
    val downloadPdfUseCase = FakeDownloadPdfUseCase()
    val presenter = InsuranceEvidenceOverviewPresenter(
      downloadPdfUseCase = downloadPdfUseCase,
    )
    return Pair(presenter, downloadPdfUseCase)
  }

  @Test
  fun `when loading email fails show failure state`() = runTest {
    val (presenter, downloadPdfUseCase) = createPresenterWithFakes()
    presenter.test(InsuranceEvidenceOverviewState.Loading) {
      sendEvent(InsuranceEvidenceOverviewEvent.OnDownloadCertificate("url"))
      skipItems(3)
      downloadPdfUseCase.resultTurbine.add(ErrorMessage().left())
      assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceOverviewState.Failure::class)
    }
  }

  @Test
  fun `when download succeeds show success state with correct uri`() = runTest {
    val (presenter, downloadPdfUseCase) = createPresenterWithFakes()
    val file = File("dummy/path")
    presenter.test(InsuranceEvidenceOverviewState.Loading) {
      sendEvent(InsuranceEvidenceOverviewEvent.OnDownloadCertificate("url"))
      skipItems(3)
      downloadPdfUseCase.resultTurbine.add(file.right())
      assertThat(awaitItem() as InsuranceEvidenceOverviewState.Success)
        .prop(InsuranceEvidenceOverviewState.Success::insuranceEvidenceUri).isEqualTo(file)
    }
  }

  @Test
  fun `when download fails first time but then succeeds on retry state becomes success`() = runTest {
    val (presenter, downloadPdfUseCase) = createPresenterWithFakes()
    val file = File("dummy/path")
    presenter.test(InsuranceEvidenceOverviewState.Loading) {
      skipItems(1)
      downloadPdfUseCase.resultTurbine.add(ErrorMessage().left())
      sendEvent(InsuranceEvidenceOverviewEvent.OnDownloadCertificate("url"))
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceOverviewState.Failure::class)
      sendEvent(InsuranceEvidenceOverviewEvent.RetryLoadData)
      downloadPdfUseCase.resultTurbine.add(file.right())
      assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceOverviewState.Loading::class)
      assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceOverviewState.Success::class)
    }
  }
}

class FakeDownloadPdfUseCase : DownloadPdfUseCase {
  val resultTurbine = Turbine<Either<ErrorMessage, File>>()

  override suspend fun invoke(url: String): Either<ErrorMessage, File> {
    return resultTurbine.awaitItem()
  }
}
