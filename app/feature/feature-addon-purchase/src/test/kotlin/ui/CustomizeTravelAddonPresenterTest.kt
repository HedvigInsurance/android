package ui

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.addon.purchase.data.AddonOffer.Selectable
import com.hedvig.android.feature.addon.purchase.data.AddonQuote
import com.hedvig.android.feature.addon.purchase.data.GenerateAddonOfferResult
import com.hedvig.android.feature.addon.purchase.data.GetAddonOfferUseCase
import com.hedvig.android.feature.addon.purchase.data.UmbrellaAddonQuote
import com.hedvig.android.feature.addon.purchase.ui.customize.CommonSuccessParameters
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeAddonState
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonEvent
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonPresenter
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class CustomizeTravelAddonPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  val insuranceId = "test"

  @Test
  fun `if receive error show error screen`() = runTest {
    val useCase = FakeGetAddonOfferUseCase()
    val presenter = CustomizeTravelAddonPresenter(
      getAddonOfferUseCase = useCase,
      insuranceId = insuranceId,
    )
    presenter.test(CustomizeAddonState.Loading) {
      skipItems(1)
      useCase.turbine.add(ErrorMessage().left())
      val state = awaitItem()
      assertThat(state).isInstanceOf(CustomizeAddonState.Failure::class)
    }
  }

  @Test
  fun `do not trigger reload if returning from success last state`() = runTest {
    val useCase = FakeGetAddonOfferUseCase()
    val presenter = CustomizeTravelAddonPresenter(
      getAddonOfferUseCase = useCase,
      insuranceId = insuranceId,
    )
    presenter.test(
      CustomizeAddonState.Success.Selectable(
        addonOffer = fakeTravelOfferTwoOptions,
        currentlyChosenOption = fakeAddonQuote1,
        currentlyChosenOptionInDialog = fakeAddonQuote1,
        chosenOptionPremiumExtra = UiMoney(0.0, UiCurrencyCode.SEK),
        currentlyActiveAddon = null,
        commonParams = fakeCommonSuccessParameters,
      ),
    ) {
      skipItems(1)
      useCase.turbine.add(ErrorMessage().left())
      expectNoEvents()
    }
  }

  @Test
  fun `if receive good response return correct data, pre-choose first addon and do not navigate further`() = runTest {
    val useCase = FakeGetAddonOfferUseCase()
    val presenter = CustomizeTravelAddonPresenter(
      getAddonOfferUseCase = useCase,
      insuranceId = insuranceId,
    )
    presenter.test(
      CustomizeAddonState.Loading,
    ) {
      skipItems(1)
      useCase.turbine.add(fakeGenerateAddonOfferResultTwoOptions.right())
      val state = awaitItem()
      assertThat(state).isInstanceOf(CustomizeAddonState.Success.Selectable::class)
        .apply {
          prop(CustomizeAddonState.Success.Selectable::commonParams)
            .prop(CommonSuccessParameters::summaryParamsToNavigateFurther)
            .isNull()
          prop(CustomizeAddonState.Success.Selectable::addonOffer).isEqualTo(fakeTravelOfferTwoOptions)
          prop(
            CustomizeAddonState.Success.Selectable::currentlyChosenOption,
          ).isEqualTo(fakeTravelOfferTwoOptions.addonOptions[0])
          prop(
            CustomizeAddonState.Success.Selectable::currentlyChosenOptionInDialog,
          ).isEqualTo(fakeTravelOfferTwoOptions.addonOptions[0])
        }
    }
  }

  @Test
  fun `if choose option in dialog show it in the dialog ui but do not change currently chosen until select button is not clicked`() =
    runTest {
      val useCase = FakeGetAddonOfferUseCase()
      val presenter = CustomizeTravelAddonPresenter(
        getAddonOfferUseCase = useCase,
        insuranceId = insuranceId,
      )
      presenter.test(
        CustomizeAddonState.Loading,
      ) {
        useCase.turbine.add(fakeGenerateAddonOfferResultTwoOptions.right())
        skipItems(2)
        sendEvent(CustomizeTravelAddonEvent.ChooseOptionInDialog(fakeAddonQuote2))
        assertThat(awaitItem()).isInstanceOf(CustomizeAddonState.Success.Selectable::class)
          .apply {
            prop(CustomizeAddonState.Success.Selectable::currentlyChosenOption).isEqualTo(fakeAddonQuote1)
            prop(CustomizeAddonState.Success.Selectable::currentlyChosenOptionInDialog).isEqualTo(fakeAddonQuote2)
          }
        sendEvent(CustomizeTravelAddonEvent.ChooseSelectedOption)
        assertThat(awaitItem()).isInstanceOf(CustomizeAddonState.Success.Selectable::class)
          .apply {
            prop(CustomizeAddonState.Success.Selectable::currentlyChosenOption).isEqualTo(fakeAddonQuote2)
            prop(CustomizeAddonState.Success.Selectable::currentlyChosenOptionInDialog).isEqualTo(fakeAddonQuote2)
          }
      }
    }

  @Test
  fun `if reload trigger new data load`() = runTest {
    val useCase = FakeGetAddonOfferUseCase()
    val presenter = CustomizeTravelAddonPresenter(
      getAddonOfferUseCase = useCase,
      insuranceId = insuranceId,
    )
    presenter.test(
      CustomizeAddonState.Loading,
    ) {
      skipItems(1)
      useCase.turbine.add(ErrorMessage().left())
      assertThat(awaitItem()).isInstanceOf(CustomizeAddonState.Failure::class)
      sendEvent(CustomizeTravelAddonEvent.Reload)
      useCase.turbine.add(fakeGenerateAddonOfferResultTwoOptions.right())
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf(CustomizeAddonState.Success.Selectable::class)
    }
  }
}

private class FakeGetAddonOfferUseCase() : GetAddonOfferUseCase {
  val turbine = Turbine<Either<ErrorMessage, GenerateAddonOfferResult>>()

  override suspend fun invoke(contractId: String): Either<ErrorMessage, GenerateAddonOfferResult> {
    return turbine.awaitItem()
  }
}

private val fakeAddonQuote1 = AddonQuote(
  addonId = "addonId1",
  displayTitle = "45 days",
  displayDetails = listOf(),
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
    monthlyGross = UiMoney(49.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(49.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
)

private val fakeAddonQuote2 = AddonQuote(
  displayTitle = "60 days",
  addonId = "addonId2",
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
  displayDetails = listOf(),
  addonSubtype = "60_DAYS",
  documents = listOf(),
  displayDescription = "Mock quote 60 days",
  itemCost = ItemCost(
    monthlyGross = UiMoney(59.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(59.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
)

private val fakeTravelOfferTwoOptions = Selectable(
  addonOptions = nonEmptyListOf(
    fakeAddonQuote1,
    fakeAddonQuote2,
  ),
  fieldTitle = "Maximum travel limit",
  selectionTitle = "Choose your maximum travel limit",
  selectionDescription = "Days covered when travelling",
)

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

private val fakeCommonSuccessParameters = CommonSuccessParameters(
  pageTitle = "Extend your coverage",
  pageDescription = "Get extra coverage when you travel abroad",
  currentTotalCost = ItemCost(
    monthlyGross = UiMoney(100.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(100.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
  umbrellaDisplayTitle = "Travel plus",
  umbrellaDisplayDescription = "For those who travel often: luggage protection and 24/7 assistance worldwide",
  quoteId = "quoteId",
  activationDate = LocalDate(2024, 12, 30),
  baseQuoteCost = ItemCost(
    monthlyGross = UiMoney(100.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(100.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
  summaryParamsToNavigateFurther = null,
  notificationMessage = null,
  productVariant = fakeProductVariant,
  contractId = "test",
  whatsIncludedPageTitle = "What is Travel Insurance Plus?",
  whatsIncludedPageDescription = "Travel Insurance Plus is extended coverage",
)

private val fakeGenerateAddonOfferResultTwoOptions = GenerateAddonOfferResult.AddonOfferResult(
  pageTitle = "Extend your coverage",
  pageDescription = "Get extra coverage when you travel abroad",
  contractId = "test",
  notificationMessage = null,
  whatsIncludedPageTitle = "What is Travel Insurance Plus?",
  whatsIncludedPageDescription = "Travel Insurance Plus is extended coverage",
  currentTotalCost = ItemCost(
    monthlyGross = UiMoney(100.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(100.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
  umbrellaAddonQuote = UmbrellaAddonQuote(
    quoteId = "quoteId",
    displayTitle = "Travel plus",
    displayDescription = "For those who travel often: luggage protection and 24/7 assistance worldwide",
    activationDate = LocalDate(2024, 12, 30),
    addonOffer = fakeTravelOfferTwoOptions,
    activeAddons = emptyList(),
    baseInsuranceCost = ItemCost(
      monthlyGross = UiMoney(100.0, UiCurrencyCode.SEK),
      monthlyNet = UiMoney(100.0, UiCurrencyCode.SEK),
      discounts = emptyList(),
    ),
    productVariant = fakeProductVariant,
  ),
)
