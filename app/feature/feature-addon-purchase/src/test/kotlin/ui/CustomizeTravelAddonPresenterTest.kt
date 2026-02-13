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
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.feature.addon.purchase.data.AddonOffer.Selectable
import com.hedvig.android.feature.addon.purchase.data.AddonQuote
import com.hedvig.android.feature.addon.purchase.data.GetAddonOfferUseCase
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
      CustomizeAddonState.SuccessSelectable(
        travelAddonSelectableOffer = fakeTravelOfferTwoOptions,
        currentlyChosenOption = fakeAddonQuote1,
        currentlyChosenOptionInDialog = fakeAddonQuote1,
        summaryParamsToNavigateFurther = null,
        currentlyActiveAddon = null,
        chosenOptionPremiumExtra = UiMoney(0.0, UiCurrencyCode.SEK),
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
      useCase.turbine.add(fakeTravelOfferTwoOptions.right())
      val state = awaitItem()
      assertThat(state).isInstanceOf(CustomizeAddonState.SuccessSelectable::class)
        .apply {
          prop(CustomizeAddonState.SuccessSelectable::summaryParamsToNavigateFurther)
            .isNull()
          prop(CustomizeAddonState.SuccessSelectable::travelAddonSelectableOffer).isEqualTo(fakeTravelOfferTwoOptions)
          prop(
            CustomizeAddonState.SuccessSelectable::currentlyChosenOption,
          ).isEqualTo(fakeTravelOfferTwoOptions.addonOptions[0])
          prop(
            CustomizeAddonState.SuccessSelectable::currentlyChosenOptionInDialog,
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
        useCase.turbine.add(fakeTravelOfferTwoOptions.right())
        skipItems(2)
        sendEvent(CustomizeTravelAddonEvent.ChooseOptionInDialog(fakeAddonQuote2))
        assertThat(awaitItem()).isInstanceOf(CustomizeAddonState.SuccessSelectable::class)
          .apply {
            prop(CustomizeAddonState.SuccessSelectable::currentlyChosenOption).isEqualTo(fakeAddonQuote1)
            prop(CustomizeAddonState.SuccessSelectable::currentlyChosenOptionInDialog).isEqualTo(fakeAddonQuote2)
          }
        sendEvent(CustomizeTravelAddonEvent.ChooseSelectedOption)
        assertThat(awaitItem()).isInstanceOf(CustomizeAddonState.SuccessSelectable::class)
          .apply {
            prop(CustomizeAddonState.SuccessSelectable::currentlyChosenOption).isEqualTo(fakeAddonQuote2)
            prop(CustomizeAddonState.SuccessSelectable::currentlyChosenOptionInDialog).isEqualTo(fakeAddonQuote2)
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
      useCase.turbine.add(fakeTravelOfferTwoOptions.right())
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf(CustomizeAddonState.SuccessSelectable::class)
    }
  }
}

private class FakeGetAddonOfferUseCase() : GetAddonOfferUseCase {
  val turbine = Turbine<Either<ErrorMessage, Selectable>>()

  override suspend fun invoke(contractId: String): Either<ErrorMessage, Selectable> {
    return turbine.awaitItem()
  }
}

private val fakeAddonQuote1 = AddonQuote(
  quoteId = "id",
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
  quoteId = "id",
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
private val fakeTravelOfferOnlyOneOption = Selectable(
  addonOptions = nonEmptyListOf(
    fakeAddonQuote1,
  ),
  title = "Travel Plus",
  description = "For those who travel often: luggage protection and 24/7 assistance worldwide",
  activationDate = LocalDate(2024, 12, 30),
  currentTravelAddon = null,
)

private val fakeTravelOfferTwoOptions = Selectable(
  addonOptions = nonEmptyListOf(
    fakeAddonQuote1,
    fakeAddonQuote2,
  ),
  title = "Travel Plus",
  description = "For those who travel often: luggage protection and 24/7 assistance worldwide",
  activationDate = LocalDate(2024, 12, 30),
  currentTravelAddon = null,
)
