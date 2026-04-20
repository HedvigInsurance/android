package ui

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.addon.purchase.data.AddonQuote
import com.hedvig.android.feature.addon.purchase.data.CurrentlyActiveAddon
import com.hedvig.android.feature.addon.purchase.data.GetInsuranceForTravelAddonUseCase
import com.hedvig.android.feature.addon.purchase.data.GetQuoteCostBreakdownUseCase
import com.hedvig.android.feature.addon.purchase.data.InsuranceForAddon
import com.hedvig.android.feature.addon.purchase.data.SubmitAddonPurchaseUseCase
import com.hedvig.android.feature.addon.purchase.navigation.AddonType
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryEvent
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryPresenter
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState
import com.hedvig.android.feature.addon.purchase.ui.summary.CostBreakdownWithExtras
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.ui.tiersandaddons.QuoteCostBreakdown
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class AddonSummaryPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `if receive error navigate to failure screen`() = runTest {
    val submitUseCase = FakeSubmitAddonPurchaseUseCase()
    val insuranceUseCase = FakeAddonSummaryGetInsuranceUseCase()
    val costBreakdownUseCase = FakeGetQuoteCostBreakdownUseCase()
    val presenter = AddonSummaryPresenter(
      summaryParameters = testSummaryParametersWithCurrentAddon,
      submitAddonPurchaseUseCase = submitUseCase,
      addonPurchaseSource = AddonBannerSource.INSURANCES_TAB,
      getQuoteCostBreakdownUseCase = costBreakdownUseCase,
      getInsuranceForTravelAddonUseCase = insuranceUseCase,
    )
    presenter.test(AddonSummaryState.Loading()) {
      skipItems(1)
      insuranceUseCase.turbine.add(listOf(fakeInsuranceForAddon).right())
      costBreakdownUseCase.turbine.add(fakeQuoteCostBreakdown.right())
      skipItems(1)
      sendEvent(AddonSummaryEvent.Submit)
      submitUseCase.turbine.add(ErrorMessage().left())
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf(AddonSummaryState.Content::class)
        .prop(AddonSummaryState.Content::navigateToFailure).isNotNull()
    }
  }

  @Test
  fun `if receive no errors navigate to success screen with activationDate from previous parameters`() = runTest {
    val submitUseCase = FakeSubmitAddonPurchaseUseCase()
    val insuranceUseCase = FakeAddonSummaryGetInsuranceUseCase()
    val costBreakdownUseCase = FakeGetQuoteCostBreakdownUseCase()
    val presenter = AddonSummaryPresenter(
      summaryParameters = testSummaryParametersWithCurrentAddon,
      submitAddonPurchaseUseCase = submitUseCase,
      addonPurchaseSource = AddonBannerSource.INSURANCES_TAB,
      getQuoteCostBreakdownUseCase = costBreakdownUseCase,
      getInsuranceForTravelAddonUseCase = insuranceUseCase,
    )
    presenter.test(AddonSummaryState.Loading()) {
      skipItems(1)
      insuranceUseCase.turbine.add(listOf(fakeInsuranceForAddon).right())
      costBreakdownUseCase.turbine.add(fakeQuoteCostBreakdown.right())
      skipItems(1)
      sendEvent(AddonSummaryEvent.Submit)
      submitUseCase.turbine.add(Unit.right())
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf(AddonSummaryState.Loading::class)
        .prop(AddonSummaryState.Loading::activationDateToNavigateToSuccess)
        .isNotNull()
        .isEqualTo(testSummaryParametersWithCurrentAddon.activationDate)
    }
  }

  @Test
  fun `the difference between current addon price and new addon price is shown correctly`() = runTest {
    val insuranceUseCase1 = FakeAddonSummaryGetInsuranceUseCase()
    val costBreakdownUseCase1 = FakeGetQuoteCostBreakdownUseCase()
    val presenter1 = AddonSummaryPresenter(
      summaryParameters = testSummaryParametersWithCurrentAddon,
      submitAddonPurchaseUseCase = FakeSubmitAddonPurchaseUseCase(),
      addonPurchaseSource = AddonBannerSource.INSURANCES_TAB,
      getQuoteCostBreakdownUseCase = costBreakdownUseCase1,
      getInsuranceForTravelAddonUseCase = insuranceUseCase1,
    )
    presenter1.test(AddonSummaryState.Loading()) {
      skipItems(1)
      insuranceUseCase1.turbine.add(listOf(fakeInsuranceForAddon).right())
      costBreakdownUseCase1.turbine.add(fakeQuoteCostBreakdown.right())
      assertThat(awaitItem()).isInstanceOf(AddonSummaryState.Content::class)
        .prop(AddonSummaryState.Content::costBreakdownWithExtras)
        .prop(CostBreakdownWithExtras::totalExtra)
        .isEqualTo(UiMoney(10.0, UiCurrencyCode.SEK))
    }

    val insuranceUseCase2 = FakeAddonSummaryGetInsuranceUseCase()
    val costBreakdownUseCase2 = FakeGetQuoteCostBreakdownUseCase()
    val presenter2 = AddonSummaryPresenter(
      summaryParameters = testSummaryParametersWithMoreExpensiveCurrentAddon,
      submitAddonPurchaseUseCase = FakeSubmitAddonPurchaseUseCase(),
      addonPurchaseSource = AddonBannerSource.INSURANCES_TAB,
      getQuoteCostBreakdownUseCase = costBreakdownUseCase2,
      getInsuranceForTravelAddonUseCase = insuranceUseCase2,
    )
    presenter2.test(AddonSummaryState.Loading()) {
      skipItems(1)
      insuranceUseCase2.turbine.add(listOf(fakeInsuranceForAddon).right())
      costBreakdownUseCase2.turbine.add(fakeQuoteCostBreakdown.right())
      assertThat(awaitItem()).isInstanceOf(AddonSummaryState.Content::class)
        .prop(AddonSummaryState.Content::costBreakdownWithExtras)
        .prop(CostBreakdownWithExtras::totalExtra)
        .isEqualTo(UiMoney(-20.0, UiCurrencyCode.SEK))
    }
  }

  @Test
  fun `if there is no current addon, total price change should show the price of the quote`() = runTest {
    val insuranceUseCase = FakeAddonSummaryGetInsuranceUseCase()
    val costBreakdownUseCase = FakeGetQuoteCostBreakdownUseCase()
    val presenter = AddonSummaryPresenter(
      summaryParameters = testSummaryParametersNoCurrentAddon,
      submitAddonPurchaseUseCase = FakeSubmitAddonPurchaseUseCase(),
      addonPurchaseSource = AddonBannerSource.INSURANCES_TAB,
      getQuoteCostBreakdownUseCase = costBreakdownUseCase,
      getInsuranceForTravelAddonUseCase = insuranceUseCase,
    )
    presenter.test(AddonSummaryState.Loading()) {
      skipItems(1)
      insuranceUseCase.turbine.add(listOf(fakeInsuranceForAddon).right())
      costBreakdownUseCase.turbine.add(fakeQuoteCostBreakdown.right())
      assertThat(awaitItem()).isInstanceOf(AddonSummaryState.Content::class)
        .prop(AddonSummaryState.Content::costBreakdownWithExtras)
        .prop(CostBreakdownWithExtras::totalExtra)
        .isEqualTo(newQuote.itemCost.monthlyNet)
    }
  }
}

private class FakeSubmitAddonPurchaseUseCase : SubmitAddonPurchaseUseCase {
  val turbine = Turbine<Either<ErrorMessage, Unit>>()

  override suspend fun invoke(quoteId: String, addonIds: List<String>): Either<ErrorMessage, Unit> {
    return turbine.awaitItem()
  }
}

private class FakeAddonSummaryGetInsuranceUseCase : GetInsuranceForTravelAddonUseCase {
  val turbine = Turbine<Either<ErrorMessage, List<InsuranceForAddon>>>()

  override suspend fun invoke(ids: List<String>): Flow<Either<ErrorMessage, List<InsuranceForAddon>>> {
    return flow { emit(turbine.awaitItem()) }
  }
}

private class FakeGetQuoteCostBreakdownUseCase : GetQuoteCostBreakdownUseCase {
  val turbine = Turbine<Either<ErrorMessage, QuoteCostBreakdown>>()

  override suspend fun invoke(
    quoteId: String,
    existingAddons: List<CurrentlyActiveAddon>,
    newAddons: List<AddonQuote>,
    baseCost: ItemCost,
    insuranceDisplayName: String,
    addonType: AddonType,
  ): Either<ErrorMessage, QuoteCostBreakdown> {
    return turbine.awaitItem()
  }
}

private val fakeProductVariant = ProductVariant(
  displayName = "SE Apartment Rent",
  contractGroup = ContractGroup.RENTAL,
  contractType = ContractType.SE_APARTMENT_RENT,
  partner = null,
  perils = emptyList(),
  insurableLimits = emptyList(),
  documents = emptyList(),
  displayTierName = null,
  tierDescription = null,
  termsVersion = "2023-01-01",
)

private val fakeInsuranceForAddon = InsuranceForAddon(
  id = "contractId",
  displayName = "SE Apartment Rent",
  contractExposure = "Bellmansgatan 19A",
  contractGroup = ContractGroup.RENTAL,
)

private val fakeQuoteCostBreakdown = QuoteCostBreakdown(
  totalMonthlyNet = UiMoney(159.0, UiCurrencyCode.SEK),
  totalMonthlyGross = UiMoney(159.0, UiCurrencyCode.SEK),
  entries = emptyList(),
)

private val newQuote = AddonQuote(
  displayTitle = "45 days",
  addonId = "addonId1",
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
  displayDescription = "Mock quote 45 days",
  itemCost = ItemCost(
    monthlyGross = UiMoney(59.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(59.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
)

private val newQuote2 = AddonQuote(
  displayTitle = "60 days",
  addonId = "addonId2",
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
  displayDescription = "Mock quote 60 days",
  itemCost = ItemCost(
    monthlyGross = UiMoney(59.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(59.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
)

private val currentAddon = CurrentlyActiveAddon(
  displayTitle = "Current Travel Addon",
  displayDescription = null,
  cost = ItemCost(
    monthlyGross = UiMoney(49.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(49.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
)

private val moreExpensiveCurrentAddon = CurrentlyActiveAddon(
  displayTitle = "Current Travel Addon",
  displayDescription = null,
  cost = ItemCost(
    monthlyGross = UiMoney(79.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(79.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
)

private val testSummaryParametersWithCurrentAddon = SummaryParameters(
  productVariant = fakeProductVariant,
  contractId = "contractId",
  baseInsuranceCost = ItemCost(
    monthlyGross = UiMoney(100.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(100.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
  chosenQuotes = listOf(newQuote),
  activationDate = LocalDate(2024, 12, 30),
  currentlyActiveAddons = listOf(currentAddon),
  quoteId = "quoteId",
  notificationMessage = null,
  addonType = AddonType.SELECTABLE,
)

private val testSummaryParametersWithMoreExpensiveCurrentAddon = SummaryParameters(
  productVariant = fakeProductVariant,
  contractId = "contractId",
  baseInsuranceCost = ItemCost(
    monthlyGross = UiMoney(100.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(100.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
  chosenQuotes = listOf(newQuote2),
  activationDate = LocalDate(2024, 12, 30),
  currentlyActiveAddons = listOf(moreExpensiveCurrentAddon),
  quoteId = "quoteId",
  notificationMessage = null,
  addonType = AddonType.SELECTABLE,
)

private val testSummaryParametersNoCurrentAddon = SummaryParameters(
  productVariant = fakeProductVariant,
  contractId = "contractId",
  baseInsuranceCost = ItemCost(
    monthlyGross = UiMoney(100.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(100.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
  chosenQuotes = listOf(newQuote),
  activationDate = LocalDate(2024, 12, 30),
  currentlyActiveAddons = emptyList(),
  quoteId = "quoteId",
  notificationMessage = null,
  addonType = AddonType.SELECTABLE,
)
