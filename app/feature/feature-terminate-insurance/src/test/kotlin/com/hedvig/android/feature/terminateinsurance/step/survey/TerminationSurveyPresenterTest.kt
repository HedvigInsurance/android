package com.hedvig.android.feature.terminateinsurance.step.survey

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isSameInstanceAs
import assertk.assertions.isTrue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleDisplayItem
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleIntent
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.Deductible
import com.hedvig.android.data.changetier.data.Tier
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.data.contract.ContractGroup.RENTAL
import com.hedvig.android.data.contract.ContractType.SE_APARTMENT_RENT
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.data.TerminationReason
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class TerminationSurveyPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()
  private val downgradeSuggestion = SurveyOptionSuggestion.Action.DowngradePriceByChangingTier(
    "description",
    "Button",
  )

  private val listOfOptionsForHome = listOf(
    TerminationSurveyOption(
      id = "id1",
      feedBackRequired = false,
      title = "I'm moving",
      subOptions = listOf(),
      listIndex = 0,
      suggestion = SurveyOptionSuggestion.Action.UpdateAddress("description", "buttonTitle"),
    ),
    TerminationSurveyOption(
      id = "id2",
      title = " I no longer need insurance",
      feedBackRequired = false,
      suggestion = null,
      listIndex = 1,
      subOptions = listOf(
        TerminationSurveyOption("id2-2", 0, "I have moved abroad", feedBackRequired = false, null, listOf()),
        TerminationSurveyOption("id2-1", 1, "Other reason", feedBackRequired = true, null, listOf()),
      ),
    ),
    TerminationSurveyOption(
      id = "id3",
      title = "- I got a better offer elsewhere",
      feedBackRequired = true,
      suggestion = null,
      listIndex = 2,
      subOptions = listOf(),
    ),
    TerminationSurveyOption(
      id = "id4",
      title = "Other reason",
      feedBackRequired = false,
      suggestion = downgradeSuggestion,
      listIndex = 3,
      subOptions = listOf(),
    ),
  )

  @Test
  fun `if tap on feedback field it would open full screen input field`() = runTest {
    val repository = FakeTerminateInsuranceRepository()
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      options = listOfOptionsForHome,
      terminateInsuranceRepository = repository,
      changeTierRepository,
    )
    presenter.test(initialState = TerminationSurveyState()) {
      assertThat(awaitItem().reasons.map { it.surveyOption }).isEqualTo(listOfOptionsForHome)
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.ShowFullScreenEditText(listOfOptionsForHome[3]))
      assertThat(awaitItem().showFullScreenEditText?.surveyOption).isEqualTo(listOfOptionsForHome[3])
    }
  }

  @Test
  fun `if full screen input field is dismissed do not show full screen input field`() = runTest {
    val repository = FakeTerminateInsuranceRepository()
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      repository,
      changeTierRepository,
    )
    presenter.test(initialState = TerminationSurveyState()) {
      skipItems(1)
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.ShowFullScreenEditText(listOfOptionsForHome[3]))
      assertThat(awaitItem().showFullScreenEditText).isNotNull()
      sendEvent(TerminationSurveyEvent.CloseFullScreenEditText)
      assertThat(awaitItem().showFullScreenEditText).isNull()
    }
  }

  @Test
  fun `the received options are displayed in the correct order`() = runTest {
    val repository = FakeTerminateInsuranceRepository()
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      repository,
      changeTierRepository,
    )
    presenter.test(initialState = TerminationSurveyState()) {
      assertThat(awaitItem().reasons.map { it.surveyOption }).isEqualTo(listOfOptionsForHome)
    }
  }

  @Test
  fun `if feedback for a reason is changed the right reason is updated with the new feedback`() = runTest {
    val repository = FakeTerminateInsuranceRepository()
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      repository,
      changeTierRepository,
    )
    presenter.test(initialState = TerminationSurveyState()) {
      skipItems(1)
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.ChangeFeedbackForSelectedReason("new feedback!"))
      assertThat(
        awaitItem().reasons.first { it.surveyOption == listOfOptionsForHome[3] }.feedBack,
      ).isEqualTo("new feedback!")
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[2]))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.ChangeFeedbackForSelectedReason("new feedback22!"))
      assertThat(
        awaitItem()
          .reasons
          .first {
            it.surveyOption == listOfOptionsForHome[2]
          }.feedBack,
      ).isEqualTo("new feedback22!")
    }
  }

  @Test
  fun `when survey is submitted the right option with the right feedback is submitted`() = runTest {
    val repository = FakeTerminateInsuranceRepository()
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      repository,
      changeTierRepository,
    )
    val nextStep = TerminateInsuranceStep.TerminateInsuranceDate(
      LocalDate(2024, 6, 1),
      LocalDate(2024, 6, 29),
    )
    presenter.test(initialState = TerminationSurveyState()) {
      skipItems(1)
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.ChangeFeedbackForSelectedReason("entirely new feedback"))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.Continue)
      assertThat(
        awaitItem().navigationStepLoadingForReason,
      ).isEqualTo(TerminationReason(listOfOptionsForHome[3], "entirely new feedback"))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when survey is submitted for option with no subOptions navigate to nex termination step`() = runTest {
    val repository = FakeTerminateInsuranceRepository()
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      repository,
      changeTierRepository,
    )
    val nextStep = TerminateInsuranceStep.TerminateInsuranceDate(
      LocalDate(2024, 6, 1),
      LocalDate(2024, 6, 29),
    )
    presenter.test(initialState = TerminationSurveyState()) {
      skipItems(1)
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.Continue)
      skipItems(1)
      repository.terminationFlowTurbine.add(nextStep.right())
      assertThat(awaitItem().nextNavigationStep).isEqualTo(SurveyNavigationStep.NavigateToNextTerminationStep(nextStep))
    }
  }

  @Test
  fun `when survey is submitted for option with subOptions navigate to next survey screen`() = runTest {
    val repository = FakeTerminateInsuranceRepository()
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      repository,
      changeTierRepository,
    )
    presenter.test(initialState = TerminationSurveyState()) {
      skipItems(1)
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[1]))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.Continue)
      assertThat(awaitItem().nextNavigationStep).isEqualTo(SurveyNavigationStep.NavigateToSubOptions)
    }
  }

  @Test
  fun `when chosen option contain suggestion to change tier disable continue`() = runTest {
    val repository = FakeTerminateInsuranceRepository()
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      repository,
      changeTierRepository,
    )
    presenter.test(initialState = TerminationSurveyState()) {
      skipItems(1)
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      val current = awaitItem()
      val currentSuggestion = current.selectedOption?.suggestion
      val currentContinueEnabled = current.continueAllowed
      assertThat(currentSuggestion).isSameInstanceAs(downgradeSuggestion)
      assertThat(currentContinueEnabled).isFalse()
    }
  }

  @Test
  fun `when repo has good intent but quotes are empty show ooops dialog and disable the option`() = runTest {
    val repository = FakeTerminateInsuranceRepository()
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      repository,
      changeTierRepository,
    )
    presenter.test(initialState = TerminationSurveyState()) {
      skipItems(1)
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      val current0 = awaitItem()
      val navig0 = current0.intentAndIdToRedirectToChangeTierFlow
      assertThat(navig0).isNull()
      changeTierRepository.changeTierIntentTurbine.add(
        ChangeTierDeductibleIntent(LocalDate(2024, 11, 15), listOf()).right(),
      )
      sendEvent(TerminationSurveyEvent.TryToDowngradePrice)
      val current = awaitItem()
      val optionNowDisabled = current.reasons.first { it.surveyOption.suggestion == downgradeSuggestion }
      val currentEmptyQuotesDialog = current.showEmptyQuotesDialog
      val currentRedirectToChangeTierIntent = current.intentAndIdToRedirectToChangeTierFlow
      assertThat(currentEmptyQuotesDialog).isTrue()
      assertThat(currentRedirectToChangeTierIntent).isNull()
      assertThat(optionNowDisabled.surveyOption.isDisabled).isTrue()
    }
  }

  @Test
  fun `when repo has good intent with non-empty quotes redirect to changeTierFlow`() = runTest {
    val repository = FakeTerminateInsuranceRepository()
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      repository,
      changeTierRepository,
    )
    presenter.test(initialState = TerminationSurveyState()) {
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      skipItems(2)
      changeTierRepository.changeTierIntentTurbine.add(
        ChangeTierDeductibleIntent(LocalDate(2024, 11, 15), listOf(testQuote)).right(),
      )
      sendEvent(TerminationSurveyEvent.TryToDowngradePrice)
      val current = awaitItem()
      val currentEmptyQuotesDialog = current.showEmptyQuotesDialog
      val currentRedirectToChangeTierIntent = current.intentAndIdToRedirectToChangeTierFlow
      assertThat(currentEmptyQuotesDialog).isFalse()
      assertThat(currentRedirectToChangeTierIntent).isNotNull()
    }
  }

  @Test
  fun `when repo gives bad response show error`() = runTest {
    val repository = FakeTerminateInsuranceRepository()
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      repository,
      changeTierRepository,
    )
    presenter.test(initialState = TerminationSurveyState()) {
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      skipItems(2)
      changeTierRepository.changeTierIntentTurbine.add(ErrorMessage().left())
      sendEvent(TerminationSurveyEvent.TryToDowngradePrice)
      val current = awaitItem()
      val error = current.errorWhileLoadingNextStep
      val currentEmptyQuotesDialog = current.showEmptyQuotesDialog
      val currentRedirectToChangeTierIntent = current.intentAndIdToRedirectToChangeTierFlow
      assertThat(currentEmptyQuotesDialog).isFalse()
      assertThat(currentRedirectToChangeTierIntent).isNull()
      assertThat(error).isTrue()
    }
  }
}

private val testQuote = TierDeductibleQuote(
  id = "id0",
  deductible = Deductible(
    UiMoney(0.0, SEK),
    deductiblePercentage = 25,
    description = "Endast en rörlig del om 25% av skadekostnaden.",
  ),
  displayItems = listOf(
    ChangeTierDeductibleDisplayItem(
      displayValue = "hhh",
      displaySubtitle = "mmm",
      displayTitle = "ioi",
    ),
  ),
  premium = UiMoney(199.0, SEK),
  tier = Tier(
    "BAS",
    tierLevel = 0,
    tierDescription = "Vårt paket med grundläggande villkor.",
    tierDisplayName = "Bas",
  ),
  productVariant = ProductVariant(
    displayName = "Test",
    contractGroup = RENTAL,
    contractType = SE_APARTMENT_RENT,
    partner = "test",
    perils = listOf(),
    insurableLimits = listOf(),
    documents = listOf(),
    displayTierName = "Bas",
    tierDescription = "Our most basic coverage",
  ),
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
    return "string"
  }
}

private class FakeTerminateInsuranceRepository : TerminateInsuranceRepository {
  val terminationFlowTurbine = Turbine<Either<ErrorMessage, TerminateInsuranceStep>>()

  override suspend fun startTerminationFlow(insuranceId: InsuranceId): Either<ErrorMessage, TerminateInsuranceStep> =
    terminationFlowTurbine.awaitItem()

  override suspend fun setTerminationDate(terminationDate: LocalDate): Either<ErrorMessage, TerminateInsuranceStep> =
    terminationFlowTurbine.awaitItem()

  override suspend fun submitReasonForCancelling(
    reason: TerminationReason,
  ): Either<ErrorMessage, TerminateInsuranceStep> = terminationFlowTurbine.awaitItem()

  override suspend fun confirmDeletion(): Either<ErrorMessage, TerminateInsuranceStep> =
    terminationFlowTurbine.awaitItem()

  override suspend fun getContractId(): String {
    return fakeContractId
  }
}

private val fakeContractId = "fakeContractId"
