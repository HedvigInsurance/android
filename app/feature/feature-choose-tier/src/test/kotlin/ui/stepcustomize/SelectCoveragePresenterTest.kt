package ui.stepcustomize

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
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
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import testQuote

class SelectCoveragePresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `when repo has quotes correctly show them`() = runTest {
    val getCurrentContractDataUseCase = FakeGetCurrentContractDataUseCase()
    val tierRepo = FakeChangeTierRepository()
    val presenter = SelectCoveragePresenter(
      params = fakeParams,
      tierRepository = tierRepo,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase
    )
    presenter.test(SelectCoverageState.Loading) {
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf<SelectCoverageState.Failure>()
    }
  //    val repository = FakeTerminateInsuranceRepository()
//    val changeTierRepository = FakeChangeTierRepository()
//    val presenter = TerminationSurveyPresenter(
//      listOfOptionsForHome,
//      repository,
//      changeTierRepository,
//    )
//    presenter.test(initialState = TerminationSurveyState()) {
//      skipItems(1)
//      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
//    }
  }
}

private class FakeGetCurrentContractDataUseCase(): GetCurrentContractDataUseCase {
  override suspend fun invoke(insuranceId: String): Either<ErrorMessage, CurrentContractData> {
    return either {CurrentContractData("exposure name")}
  }
}

private val fakeParams = InsuranceCustomizationParameters(
  activationDate = LocalDate(2025,9,11),
  insuranceId = "testId",
  quoteIds = listOf("id1", "id2")
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
    return listOf(testQuote)
  }

  override suspend fun addQuotesToDb(quotes: List<TierDeductibleQuote>) {
  }

  override suspend fun submitChangeTierQuote(quoteId: String): Either<ErrorMessage, Unit> {
    return either {}
  }

  override suspend fun getCurrentQuoteId(): String {
    return "string"
  }
}
