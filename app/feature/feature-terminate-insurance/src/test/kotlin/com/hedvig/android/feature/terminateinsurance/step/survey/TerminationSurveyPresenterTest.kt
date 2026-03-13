package com.hedvig.android.feature.terminateinsurance.step.survey

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
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
import com.hedvig.android.data.changetier.data.DeflectOutput
import com.hedvig.android.data.changetier.data.IntentOutput
import com.hedvig.android.data.changetier.data.Tier
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.data.changetier.data.TotalCost
import com.hedvig.android.data.contract.ContractGroup.RENTAL
import com.hedvig.android.data.contract.ContractType.SE_APARTMENT_RENT
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.terminateinsurance.data.ExtraCoverageItem
import com.hedvig.android.feature.terminateinsurance.data.SuggestionType
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
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

  private val downgradeSuggestion = SurveyOptionSuggestion(
    type = SuggestionType.DOWNGRADE_PRICE,
    description = "Check if you can get a better price",
    url = null,
  )

  private val testAction = TerminationAction.TerminateWithDate(
    minDate = LocalDate(2024, 6, 1),
    maxDate = LocalDate(2024, 6, 29),
    extraCoverageItems = emptyList(),
  )

  private val listOfOptionsForHome = listOf(
    TerminationSurveyOption(
      id = "id1",
      feedbackRequired = false,
      title = "I'm moving",
      subOptions = emptyList(),
      listIndex = 0,
      suggestion = SurveyOptionSuggestion(
        type = SuggestionType.UPDATE_ADDRESS,
        description = "Update your address instead",
        url = null,
      ),
    ),
    TerminationSurveyOption(
      id = "id2",
      title = " I no longer need insurance",
      feedbackRequired = false,
      suggestion = null,
      listIndex = 1,
      subOptions = listOf(
        TerminationSurveyOption("id2-2", 0, "I have moved abroad", false, null, emptyList()),
        TerminationSurveyOption("id2-1", 1, "Other reason", true, null, emptyList()),
      ),
    ),
    TerminationSurveyOption(
      id = "id3",
      title = "- I got a better offer elsewhere",
      feedbackRequired = true,
      suggestion = null,
      listIndex = 2,
      subOptions = emptyList(),
    ),
    TerminationSurveyOption(
      id = "id4",
      title = "Other reason",
      feedbackRequired = false,
      suggestion = downgradeSuggestion,
      listIndex = 3,
      subOptions = emptyList(),
    ),
  )

  @Test
  fun `if tap on feedback field it would open full screen input field`() = runTest {
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      options = listOfOptionsForHome,
      action = testAction,
      changeTierRepository = changeTierRepository,
      contractId = "contractId",
    )
    presenter.test(initialState = TerminationSurveyState(listOfOptionsForHome)) {
      assertThat(awaitItem().reasons).isEqualTo(listOfOptionsForHome)
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.ShowFullScreenEditText)
      assertThat(awaitItem().showFullScreenEditText).isTrue()
    }
  }

  @Test
  fun `if full screen input field is dismissed do not show full screen input field`() = runTest {
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      testAction,
      changeTierRepository,
      "contractId",
    )
    presenter.test(initialState = TerminationSurveyState(listOfOptionsForHome)) {
      skipItems(1)
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.ShowFullScreenEditText)
      assertThat(awaitItem().showFullScreenEditText).isNotNull()
      sendEvent(TerminationSurveyEvent.CloseFullScreenEditText)
      assertThat(awaitItem().showFullScreenEditText).isFalse()
    }
  }

  @Test
  fun `the received options are displayed in the correct order`() = runTest {
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      testAction,
      changeTierRepository,
      "contractId",
    )
    presenter.test(initialState = TerminationSurveyState(listOfOptionsForHome)) {
      assertThat(awaitItem().reasons).isEqualTo(listOfOptionsForHome)
    }
  }

  @Test
  fun `if feedback for a reason is changed the screen-wide feedback text is updated with the new input`() = runTest {
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      testAction,
      changeTierRepository,
      "contractId",
    )
    presenter.test(initialState = TerminationSurveyState(listOfOptionsForHome)) {
      skipItems(1)
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.EditTextFeedback("new feedback!"))
      assertThat(awaitItem().feedbackText).isEqualTo("new feedback!")
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[2]))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.EditTextFeedback("new feedback22!"))
      assertThat(awaitItem().feedbackText).isEqualTo("new feedback22!")
    }
  }

  @Test
  fun `when survey is submitted for option with no subOptions navigate to next termination step`() = runTest {
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      testAction,
      changeTierRepository,
      "contractId",
    )
    presenter.test(initialState = TerminationSurveyState(listOfOptionsForHome)) {
      skipItems(1)
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[2]))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.EditTextFeedback("my feedback"))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.Continue)
      val result = awaitItem()
      assertThat(result.nextNavigationStep)
        .isInstanceOf<SurveyNavigationStep.NavigateToNextTerminationStep>()
      val navStep = result.nextNavigationStep as SurveyNavigationStep.NavigateToNextTerminationStep
      assertThat(navStep.selectedOption).isEqualTo(listOfOptionsForHome[2])
      assertThat(navStep.feedbackText).isEqualTo("my feedback")
      assertThat(navStep.action).isEqualTo(testAction)
    }
  }

  @Test
  fun `when survey is submitted for option with subOptions navigate to next survey screen`() = runTest {
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      testAction,
      changeTierRepository,
      "contractId",
    )
    presenter.test(initialState = TerminationSurveyState(listOfOptionsForHome)) {
      skipItems(1)
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[1]))
      skipItems(1)
      sendEvent(TerminationSurveyEvent.Continue)
      assertThat(awaitItem().nextNavigationStep).isEqualTo(SurveyNavigationStep.NavigateToSubOptions)
    }
  }

  @Test
  fun `when chosen option contain suggestion to change tier disable continue`() = runTest {
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      testAction,
      changeTierRepository,
      "contractId",
    )
    presenter.test(initialState = TerminationSurveyState(listOfOptionsForHome)) {
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
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      testAction,
      changeTierRepository,
      "contractId",
    )
    presenter.test(initialState = TerminationSurveyState(listOfOptionsForHome)) {
      skipItems(1)
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      val current0 = awaitItem()
      val navig0 = current0.intentAndIdToRedirectToChangeTierFlow
      assertThat(navig0).isNull()
      changeTierRepository.changeTierIntentTurbine.add(
        ChangeTierDeductibleIntent(
          IntentOutput(
            LocalDate(2024, 11, 15),
            emptyList(),
          ),
          null,
        ).right(),
      )
      sendEvent(TerminationSurveyEvent.TryToDowngradePrice)
      val current = awaitItem()
      val optionNowDisabled = current.reasons.first { it.suggestion == downgradeSuggestion }
      val currentEmptyQuotesDialog = current.showEmptyQuotesDialog
      val currentRedirectToChangeTierIntent = current.intentAndIdToRedirectToChangeTierFlow
      assertThat(currentEmptyQuotesDialog).isNotNull()
      assertThat(currentRedirectToChangeTierIntent).isNull()
      assertThat(optionNowDisabled.isDisabled).isTrue()
    }
  }

  @Test
  fun `when repo has good intent with non-empty quotes redirect to changeTierFlow`() = runTest {
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      testAction,
      changeTierRepository,
      "contractId",
    )
    presenter.test(initialState = TerminationSurveyState(listOfOptionsForHome)) {
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      skipItems(2)
      changeTierRepository.changeTierIntentTurbine.add(
        ChangeTierDeductibleIntent(
          IntentOutput(
            LocalDate(2024, 11, 15),
            listOf(testQuote),
          ),
          null,
        ).right(),
      )
      sendEvent(TerminationSurveyEvent.TryToDowngradePrice)
      val current = awaitItem()
      val currentEmptyQuotesDialog = current.showEmptyQuotesDialog
      val currentRedirectToChangeTierIntent = current.intentAndIdToRedirectToChangeTierFlow
      assertThat(currentEmptyQuotesDialog).isNull()
      assertThat(currentRedirectToChangeTierIntent).isNotNull()
    }
  }

  @Test
  fun `when repo gives bad response show error`() = runTest {
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      testAction,
      changeTierRepository,
      "contractId",
    )
    presenter.test(initialState = TerminationSurveyState(listOfOptionsForHome)) {
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      skipItems(2)
      changeTierRepository.changeTierIntentTurbine.add(ErrorMessage().left())
      sendEvent(TerminationSurveyEvent.TryToDowngradePrice)
      val current = awaitItem()
      val error = current.errorWhileLoadingNextStep
      val currentEmptyQuotesDialog = current.showEmptyQuotesDialog
      val currentRedirectToChangeTierIntent = current.intentAndIdToRedirectToChangeTierFlow
      assertThat(currentEmptyQuotesDialog).isNull()
      assertThat(currentRedirectToChangeTierIntent).isNull()
      assertThat(error).isTrue()
    }
  }

  @Test
  fun `when deflectOutput is returned show deflect dialog and do not redirect to change tier flow`() = runTest {
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      testAction,
      changeTierRepository,
      "contractId",
    )
    presenter.test(initialState = TerminationSurveyState(listOfOptionsForHome)) {
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      skipItems(2)
      changeTierRepository.changeTierIntentTurbine.add(
        ChangeTierDeductibleIntent(
          intentOutput = null,
          deflectOutput = DeflectOutput(
            title = "Deflect title",
            message = "Deflect msg",
          ),
        ).right(),
      )
      sendEvent(TerminationSurveyEvent.TryToDowngradePrice)
      val current = awaitItem()
      val currentDeflectDialog = current.showEmptyQuotesDialog
      val currentRedirectToChangeTierIntent = current.intentAndIdToRedirectToChangeTierFlow
      val currentSelectedOptionId = current.selectedOptionId
      assertThat(currentDeflectDialog).isNotNull().isInstanceOf<DeflectType.Deflect>()
      val deflectType = currentDeflectDialog as DeflectType.Deflect
      assertThat(deflectType.title).isEqualTo("Deflect title")
      assertThat(currentRedirectToChangeTierIntent).isNull()
      assertThat(currentSelectedOptionId).isNull()
    }
  }

  @Test
  fun `when both intentOutput and deflectOutput are returned deflect takes precedence`() = runTest {
    val changeTierRepository = FakeChangeTierRepository()
    val presenter = TerminationSurveyPresenter(
      listOfOptionsForHome,
      testAction,
      changeTierRepository,
      "contractId",
    )
    presenter.test(initialState = TerminationSurveyState(listOfOptionsForHome)) {
      sendEvent(TerminationSurveyEvent.SelectOption(listOfOptionsForHome[3]))
      skipItems(2)
      changeTierRepository.changeTierIntentTurbine.add(
        ChangeTierDeductibleIntent(
          intentOutput = IntentOutput(
            LocalDate(2024, 11, 15),
            listOf(testQuote),
          ),
          deflectOutput = DeflectOutput(
            title = "Deflect title",
            message = "Deflect message",
          ),
        ).right(),
      )
      sendEvent(TerminationSurveyEvent.TryToDowngradePrice)
      val current = awaitItem()
      val currentDeflectDialog = current.showEmptyQuotesDialog
      val currentRedirectToChangeTierIntent = current.intentAndIdToRedirectToChangeTierFlow
      assertThat(currentDeflectDialog).isNotNull().isInstanceOf<DeflectType.Deflect>()
      val deflectType = currentDeflectDialog as DeflectType.Deflect
      assertThat(deflectType.title).isEqualTo("Deflect title")
      assertThat(currentRedirectToChangeTierIntent).isNull()
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
  tier = Tier(
    "BAS",
    tierLevel = 0,
    tierDescription = "Vårt paket med grundläggande villkor.",
    tierDisplayName = "Bas",
  ),
  addons = emptyList(),
  productVariant = ProductVariant(
    displayName = "Test",
    contractGroup = RENTAL,
    contractType = SE_APARTMENT_RENT,
    partner = "test",
    perils = emptyList(),
    insurableLimits = emptyList(),
    documents = emptyList(),
    displayTierName = "Bas",
    tierDescription = "Our most basic coverage",
    termsVersion = "termsVersion",
  ),
  currentTotalCost = TotalCost(
    monthlyGross = UiMoney(250.0, SEK),
    monthlyNet = UiMoney(200.0, SEK),
  ),
  newTotalCost = TotalCost(
    monthlyGross = UiMoney(380.0, SEK),
    monthlyNet = UiMoney(304.0, SEK),
  ),
  costBreakdown = emptyList(),
  info = null,
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

  override suspend fun addQuotesToStorage(quotes: List<TierDeductibleQuote>) {
  }

  override suspend fun submitChangeTierQuote(quoteId: String): Either<ErrorMessage, Unit> {
    return either {}
  }

  override suspend fun getCurrentQuoteId(): String {
    return "string"
  }
}
