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
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.addons.data.TravelAddonBannerSource
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
      addonPurchaseSource = TravelAddonBannerSource.INSURANCES_TAB,
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
      addonPurchaseSource = TravelAddonBannerSource.INSURANCES_TAB,
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
    assertThat(
      diff,
    ).isEqualTo(UiMoney(10.0, testSummaryParametersWithCurrentAddon.quote.itemCost.monthlyNet.currencyCode))
    val params2 = testSummaryParametersWithMoreExpensiveCurrentAddon
    val diff2 = getInitialState(params2).totalPriceChange
    assertThat(
      diff2,
    ).isEqualTo(UiMoney(-20.0, testSummaryParametersWithCurrentAddon.quote.itemCost.monthlyNet.currencyCode))
  }

  @Test
  fun `if there is no current addon, total price change should show the price of the quote`() = runTest {
    val params = testSummaryParametersNoCurrentAddon
    val diff = getInitialState(params).totalPriceChange
    assertThat(diff).isEqualTo(testSummaryParametersNoCurrentAddon.quote.itemCost.monthlyNet)
  }
}

private class FakeSubmitAddonPurchaseUseCase() : SubmitAddonPurchaseUseCase {
  val turbine = Turbine<Either<ErrorMessage, Unit>>()

  override suspend fun invoke(quoteId: String, addonId: String): Either<ErrorMessage, Unit> {
    return turbine.awaitItem()
  }
}

private val newQuote = TravelAddonQuote(
  displayName = "45 days",
  addonId = "addonId1",
  quoteId = "id",
  displayDetails = listOf(
    "Amount of insured people" to "You +1",
    "Coverage" to "45 days",
  ),
  addonVariant = AddonVariant(
    termsVersion = "terms",
    displayName = "45 days",
    product = "",
    perils = listOf(),
    documents = listOf(
      InsuranceVariantDocument(
        "Terms and Conditions",
        "url",
        InsuranceVariantDocument.InsuranceDocumentType.TERMS_AND_CONDITIONS,
      ),
    ),
  ),
  addonSubtype = "45_DAYS",
  documents = listOf(),
  displayNameLong = "Mock quote 60 days",
  itemCost = ItemCost(
    monthlyGross = UiMoney(59.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(59.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
)

private val newQuote2 = TravelAddonQuote(
  displayName = "60 days",
  addonId = "addonId2",
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
    documents = listOf(
      InsuranceVariantDocument(
        "Terms and Conditions",
        "url",
        InsuranceVariantDocument.InsuranceDocumentType.TERMS_AND_CONDITIONS,
      ),
    ),
  ),
  addonSubtype = "60_DAYS",
  documents = listOf(),
  displayNameLong = "Mock quote 60 days",
  itemCost = ItemCost(
    monthlyGross = UiMoney(59.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(59.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
)

private val currentAddon = CurrentTravelAddon(
  listOf("Coverage" to "45 days", "Insured people" to "You+1"),
  displayNameLong = "Current Travel Addon",
  netPremium = UiMoney(49.0, UiCurrencyCode.SEK),
)

private val moreExpensiveCurrentAddon = CurrentTravelAddon(
  listOf("Coverage" to "45 days", "Insured people" to "You+1"),
  displayNameLong = "Current Travel Addon",
  netPremium = UiMoney(79.0, UiCurrencyCode.SEK),
)

private val testSummaryParametersWithCurrentAddon = SummaryParameters(
  offerDisplayName = "fakeTravelOfferOnlyOneOption.title",
  quote = newQuote,
  activationDate = LocalDate(2024, 12, 30),
  currentTravelAddon = currentAddon,
)

private val testSummaryParametersWithMoreExpensiveCurrentAddon = SummaryParameters(
  offerDisplayName = "fakeTravelOfferOnlyOneOption.title",
  quote = newQuote2,
  activationDate = LocalDate(2024, 12, 30),
  currentTravelAddon = moreExpensiveCurrentAddon,
)

private val testSummaryParametersNoCurrentAddon = SummaryParameters(
  offerDisplayName = "fakeTravelOfferOnlyOneOption.title",
  quote = newQuote,
  activationDate = LocalDate(2024, 12, 30),
  currentTravelAddon = null,
)
