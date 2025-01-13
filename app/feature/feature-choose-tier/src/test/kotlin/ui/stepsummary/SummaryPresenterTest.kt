package ui.stepsummary

import FakeChangeTierRepository
import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.change.tier.data.CurrentContractData
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCase
import com.hedvig.android.feature.change.tier.navigation.SummaryParameters
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryPresenter
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Loading
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import currentQuote
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import testQuote
import testQuoteWithOneAddon
import testQuoteWithTwoAddons

class SummaryPresenterTest {
  val summaryParams = SummaryParameters(
    quoteIdToSubmit = "quoteId",
    insuranceId = "insuranceId",
    activationDate = LocalDate(2026, 1, 1),
  )

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `the uiState total is correctly calculated`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val useCase = FakeGetCurrentContractDataUseCase()
    val presenter = SummaryPresenter(
      tierRepository = tierRepo,
      params = summaryParams,
      getCurrentContractDataUseCase = useCase,
    )
    presenter.test(Loading) {
      useCase.turbine.add(CurrentContractData("currentExposureName").right())
      tierRepo.quoteTurbine.add(testQuoteWithOneAddon.right())
      tierRepo.quoteTurbine.add(currentQuote.right())
      skipItems(1)
      val state = awaitItem()
      assertThat(state)
        .isInstanceOf(SummaryState.Success::class)
        .prop(SummaryState.Success::total)
        .isEqualTo(UiMoney(235.0, com.hedvig.android.core.uidata.UiCurrencyCode.SEK))
    }
    presenter.test(Loading) {
      useCase.turbine.add(CurrentContractData("currentExposureName").right())
      tierRepo.quoteTurbine.add(testQuoteWithTwoAddons.right())
      tierRepo.quoteTurbine.add(currentQuote.right())
      skipItems(1)
      val state = awaitItem()
      assertThat(state)
        .isInstanceOf(SummaryState.Success::class)
        .prop(SummaryState.Success::total)
        .isEqualTo(UiMoney(315.0, com.hedvig.android.core.uidata.UiCurrencyCode.SEK))
    }
    presenter.test(Loading) {
      useCase.turbine.add(CurrentContractData("currentExposureName").right())
      tierRepo.quoteTurbine.add(testQuote.right())
      tierRepo.quoteTurbine.add(currentQuote.right())
      skipItems(1)
      val state = awaitItem()
      assertThat(state)
        .isInstanceOf(SummaryState.Success::class)
        .prop(SummaryState.Success::total)
        .isEqualTo(UiMoney(299.0, com.hedvig.android.core.uidata.UiCurrencyCode.SEK))
    }
  }
}

private class FakeGetCurrentContractDataUseCase : GetCurrentContractDataUseCase {
  val turbine = Turbine<Either<ErrorMessage, CurrentContractData>>()

  override suspend fun invoke(insuranceId: String): Either<ErrorMessage, CurrentContractData> {
    return turbine.awaitItem()
  }
}
