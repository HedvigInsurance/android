package ui

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.feature.addon.purchase.data.CurrentTravelAddon
import com.hedvig.android.feature.addon.purchase.data.SubmitAddonPurchaseUseCase
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuote
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryEvent
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryPresenter
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState
import com.hedvig.android.feature.addon.purchase.ui.summary.getInitialState
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class AddonSummaryPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `if receive error navigate to failure screen`() = runTest {
    val useCase = FakeSubmitAddonPurchaseUseCase()
    val presenter = AddonSummaryPresenter(
      submitAddonPurchaseUseCase = useCase,
      summaryParameters = testSummaryParametersWithCurrentAddon,
    )
    presenter.test(getInitialState(testSummaryParametersWithCurrentAddon)) {
      skipItems(1)
      sendEvent(AddonSummaryEvent.Submit)
      useCase.turbine.add(ErrorMessage().left())
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf(AddonSummaryState.Content::class)
        .prop(AddonSummaryState.Content::navigateToFailure).isTrue()
    }
  }

  @Test
  fun `if receive no errors navigate to success screen with activationDate from previous parameters`() = runTest {
    val useCase = FakeSubmitAddonPurchaseUseCase()
    val presenter = AddonSummaryPresenter(
      submitAddonPurchaseUseCase = useCase,
      summaryParameters = testSummaryParametersWithCurrentAddon,
    )
    presenter.test(getInitialState(testSummaryParametersWithCurrentAddon)) {
      skipItems(1)
      sendEvent(AddonSummaryEvent.Submit)
      useCase.turbine.add(Unit.right())
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf(AddonSummaryState.Content::class)
        .prop(AddonSummaryState.Content::activationDateForSuccessfullyPurchasedAddon)
        .isNotNull()
        .isEqualTo(testSummaryParametersWithCurrentAddon.activationDate)
    }
  }

  @Test
  fun `the difference between current addon price and new addon price is shown correctly`() = runTest {
    val params = testSummaryParametersWithCurrentAddon
    val diff = getInitialState(params).totalPriceChange
    assertThat(diff).isEqualTo(UiMoney(11.0, testSummaryParametersWithCurrentAddon.quote.price.currencyCode))
    val params2 = testSummaryParametersWithMoreExpensiveCurrentAddon
    val diff2 = getInitialState(params2).totalPriceChange
    assertThat(
      diff2,
    ).isEqualTo(UiMoney(-19.0, testSummaryParametersWithMoreExpensiveCurrentAddon.quote.price.currencyCode))
  }

  @Test
  fun `if there is no current addon, total price change should show the price of the quote`() = runTest {
    val params = testSummaryParametersNoCurrentAddon
    val diff = getInitialState(params).totalPriceChange
    assertThat(diff).isEqualTo(testSummaryParametersNoCurrentAddon.quote.price)
  }
}

private class FakeSubmitAddonPurchaseUseCase() : SubmitAddonPurchaseUseCase {
  val turbine = Turbine<Either<ErrorMessage, Unit>>()

  override suspend fun invoke(quoteId: String, addonId: String): Either<ErrorMessage, Unit> {
    return turbine.awaitItem()
  }
}

private val newQuote = TravelAddonQuote(
  displayName = "60 days",
  addonId = "addonId1",
  quoteId = "id",
  displayDetails = listOf(
    "Amount of insured people" to "You +1",
    "Coverage" to "60 days",
  ),
  addonVariant = AddonVariant(
    termsVersion = "terms",
    displayName = "45 days",
    product = "",
    perils = listOf(),
    insurableLimits = listOf(),
    documents = listOf(
      InsuranceVariantDocument(
        "Terms and Conditions",
        "url",
        InsuranceVariantDocument.InsuranceDocumentType.TERMS_AND_CONDITIONS,
      ),
    ),
  ),
  price = UiMoney(
    60.0,
    UiCurrencyCode.SEK,
  ),
)

private val newQuote2 = TravelAddonQuote(
  displayName = "60 days",
  addonId = "addonId1",
  quoteId = "id",
  displayDetails = listOf(
    "Amount of insured people" to "You +1",
    "Coverage" to "60 days",
  ),
  addonVariant = AddonVariant(
    termsVersion = "terms",
    displayName = "60 days",
    product = "",
    perils = listOf(),
    insurableLimits = listOf(),
    documents = listOf(
      InsuranceVariantDocument(
        "Terms and Conditions",
        "url",
        InsuranceVariantDocument.InsuranceDocumentType.TERMS_AND_CONDITIONS,
      ),
    ),
  ),
  price = UiMoney(
    60.0,
    UiCurrencyCode.NOK,
  ),
)

private val currentAddon = CurrentTravelAddon(
  UiMoney(49.0, UiCurrencyCode.SEK),
  listOf("Coverage" to "45 days", "Insured people" to "You+1"),
)

private val moreExpensiveCurrentAddon = CurrentTravelAddon(
  UiMoney(79.0, UiCurrencyCode.SEK),
  listOf("Coverage" to "45 days", "Insured people" to "You+1"),
)

private val testSummaryParametersWithCurrentAddon = SummaryParameters(
  offerDisplayName = "fakeTravelOfferOnlyOneOption.title",
  quote = newQuote,
  activationDate = LocalDate(2024, 12, 30),
  currentTravelAddon = currentAddon,
  popCustomizeDestination = true,
)

private val testSummaryParametersWithMoreExpensiveCurrentAddon = SummaryParameters(
  offerDisplayName = "fakeTravelOfferOnlyOneOption.title",
  quote = newQuote2,
  activationDate = LocalDate(2024, 12, 30),
  currentTravelAddon = moreExpensiveCurrentAddon,
  popCustomizeDestination = true,
)

private val testSummaryParametersNoCurrentAddon = SummaryParameters(
  offerDisplayName = "fakeTravelOfferOnlyOneOption.title",
  quote = newQuote,
  activationDate = LocalDate(2024, 12, 30),
  currentTravelAddon = null,
  popCustomizeDestination = true,
)
