package ui.stepcustomize

import CURRENT_ID
import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.raise.either
import assertk.assertThat
import assertk.assertions.isInstanceOf
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleIntent
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.feature.change.tier.data.CurrentContractData
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCase
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoveragePresenter
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import currentQuote
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import testQuote
import testQuote2

class SelectCoveragePresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `when sending quoteIds that are not stored show Failure`() = runTest {
    val getCurrentContractDataUseCase = FakeGetCurrentContractDataUseCase()
    val tierRepo = FakeChangeTierRepository()
    val presenter = SelectCoveragePresenter(
      params = params,
      tierRepository = tierRepo,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
    )
    presenter.test(SelectCoverageState.Loading) {
      tierRepo.quoteListTurbine.add(listOf())
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf<SelectCoverageState.Failure>()
    }
  }

  @Test
  fun `when sending quoteIds that are stored show Success`() = runTest {
    val getCurrentContractDataUseCase = FakeGetCurrentContractDataUseCase()
    val tierRepo = FakeChangeTierRepository()
    val presenter = SelectCoveragePresenter(
      params = params,
      tierRepository = tierRepo,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
    )
    presenter.test(SelectCoverageState.Loading) {
      tierRepo.quoteListTurbine.add(listOf(testQuote, testQuote2, currentQuote))
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf<SelectCoverageState.Success>()
    }
  }
}

private class FakeGetCurrentContractDataUseCase() : GetCurrentContractDataUseCase {
  override suspend fun invoke(insuranceId: String): Either<ErrorMessage, CurrentContractData> {
    return either { CurrentContractData("exposure name") }
  }
}

private val params = InsuranceCustomizationParameters(
  activationDate = LocalDate(2025, 9, 11),
  insuranceId = "testId",
  quoteIds = listOf("id0", "id1", CURRENT_ID),
)

private class FakeChangeTierRepository() : ChangeTierRepository {
  val changeTierIntentTurbine = Turbine<Either<ErrorMessage, ChangeTierDeductibleIntent>>()
  val quoteTurbine = Turbine<Either<ErrorMessage, TierDeductibleQuote>>()
  val quoteListTurbine = Turbine<List<TierDeductibleQuote>>()

  override suspend fun startChangeTierIntentAndGetQuotesId(
    insuranceId: String,
    source: ChangeTierCreateSource,
  ): Either<ErrorMessage, ChangeTierDeductibleIntent> {
    return changeTierIntentTurbine.awaitItem()
  }

  override suspend fun getQuoteById(id: String): Either<ErrorMessage, TierDeductibleQuote> {
    return quoteTurbine.awaitItem()
  }

  override suspend fun getQuotesById(ids: List<String>): List<TierDeductibleQuote> {
    return quoteListTurbine.awaitItem()
  }

  override suspend fun addQuotesToDb(quotes: List<TierDeductibleQuote>) {
  }

  override suspend fun submitChangeTierQuote(quoteId: String): Either<ErrorMessage, Unit> {
    return either {}
  }

  override suspend fun getCurrentQuoteId(): String {
    return CURRENT_ID
  }
}
