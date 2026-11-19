package com.hedvig.android.feature.greencard.ui.history

import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.core.fileupload.DownloadedFile
import com.hedvig.android.feature.greencard.data.GetGreenCardSpecificationsUseCase
import com.hedvig.android.feature.greencard.data.GetGreenCardsUseCase
import com.hedvig.android.feature.greencard.data.GreenCard
import com.hedvig.android.feature.greencard.data.GreenCardSpecifications
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

internal class GreenCardHistoryPresenterTest {
  @get:Rule
  val testLogcatRule = TestLogcatLoggingRule()

  @Test
  fun `with the flag off, the screen is not available and nothing is queried`() = runTest {
    val getGreenCards = CountingGetGreenCardsUseCase()
    val presenter = presenter(isFlagEnabled = false, getGreenCards = getGreenCards)
    presenter.test(GreenCardHistoryUiState.Loading) {
      skipItems(1)
      assertThat(awaitItem()).isEqualTo(GreenCardHistoryUiState.NotAvailable)
    }
    assertThat(getGreenCards.calls).isEqualTo(0)
  }

  @Test
  fun `with the flag on, the green cards load`() = runTest {
    val presenter = presenter(isFlagEnabled = true)
    presenter.test(GreenCardHistoryUiState.Loading) {
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf<GreenCardHistoryUiState.Success>()
    }
  }

  private fun presenter(isFlagEnabled: Boolean, getGreenCards: GetGreenCardsUseCase = CountingGetGreenCardsUseCase()) =
    GreenCardHistoryPresenter(
      getGreenCardsUseCase = getGreenCards,
      getGreenCardSpecificationsUseCase = object : GetGreenCardSpecificationsUseCase {
        override suspend fun invoke() = GreenCardSpecifications(emptyList()).right()
      },
      downloadPdfUseCase = object : DownloadPdfUseCase {
        override suspend fun invoke(url: String): Either<ErrorMessage, DownloadedFile> = error("Not used")
      },
      featureManager = FakeFeatureManager(mapOf(Feature.ENABLE_GREEN_CARD to isFlagEnabled)),
    )

  private class CountingGetGreenCardsUseCase : GetGreenCardsUseCase {
    var calls = 0

    override suspend fun invoke(): Either<ErrorMessage, List<GreenCard>> {
      calls++
      return emptyList<GreenCard>().right()
    }
  }
}
