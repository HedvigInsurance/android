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
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.feature.addon.purchase.data.Addon.TravelAddonOffer
import com.hedvig.android.feature.addon.purchase.data.GetTravelAddonOfferUseCase
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuote
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonEvent
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonPresenter
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonState
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
    val useCase = FakeGetTravelAddonOfferUseCase()
    val presenter = CustomizeTravelAddonPresenter(
      getTravelAddonOfferUseCase = useCase,
      insuranceId = insuranceId,
    )
    presenter.test(CustomizeTravelAddonState.Loading) {
      skipItems(1)
      useCase.turbine.add(ErrorMessage().left())
      val state = awaitItem()
      assertThat(state).isInstanceOf(CustomizeTravelAddonState.Failure::class)
    }
  }

  @Test
  fun `do not trigger reload if returning from success last state`() = runTest {
    val useCase = FakeGetTravelAddonOfferUseCase()
    val presenter = CustomizeTravelAddonPresenter(
      getTravelAddonOfferUseCase = useCase,
      insuranceId = insuranceId,
    )
    presenter.test(
      CustomizeTravelAddonState.Success(
        travelAddonOffer = fakeTravelOfferTwoOptions,
        currentlyChosenOption = fakeTravelAddonQuote1,
        currentlyChosenOptionInDialog = fakeTravelAddonQuote1,
        summaryParamsToNavigateFurther = null,
      ),
    ) {
      skipItems(1)
      useCase.turbine.add(ErrorMessage().left())
      expectNoEvents()
    }
  }

  @Test
  fun `if receive good response but only one addon redirect to next screen and pop this destination`() = runTest {
    val useCase = FakeGetTravelAddonOfferUseCase()
    val presenter = CustomizeTravelAddonPresenter(
      getTravelAddonOfferUseCase = useCase,
      insuranceId = insuranceId,
    )
    presenter.test(
      CustomizeTravelAddonState.Loading,
    ) {
      skipItems(1)
      useCase.turbine.add(fakeTravelOfferOnlyOneOption.right())
      val state = awaitItem()
      assertThat(state).isInstanceOf(CustomizeTravelAddonState.Success::class)
        .apply {
          prop(CustomizeTravelAddonState.Success::summaryParamsToNavigateFurther)
            .isEqualTo(
              SummaryParameters(
                offerDisplayName = fakeTravelOfferOnlyOneOption.title,
                quote = fakeTravelOfferOnlyOneOption.addonOptions[0],
                activationDate = fakeTravelOfferOnlyOneOption.activationDate,
                currentTravelAddon = fakeTravelOfferOnlyOneOption.currentTravelAddon,
              ),
            )
        }
    }
  }

  @Test
  fun `if receive good response return correct data, pre-choose first addon and do not navigate further`() = runTest {
    val useCase = FakeGetTravelAddonOfferUseCase()
    val presenter = CustomizeTravelAddonPresenter(
      getTravelAddonOfferUseCase = useCase,
      insuranceId = insuranceId,
    )
    presenter.test(
      CustomizeTravelAddonState.Loading,
    ) {
      skipItems(1)
      useCase.turbine.add(fakeTravelOfferTwoOptions.right())
      val state = awaitItem()
      assertThat(state).isInstanceOf(CustomizeTravelAddonState.Success::class)
        .apply {
          prop(CustomizeTravelAddonState.Success::summaryParamsToNavigateFurther)
            .isNull()
          prop(CustomizeTravelAddonState.Success::travelAddonOffer).isEqualTo(fakeTravelOfferTwoOptions)
          prop(
            CustomizeTravelAddonState.Success::currentlyChosenOption,
          ).isEqualTo(fakeTravelOfferTwoOptions.addonOptions[0])
          prop(
            CustomizeTravelAddonState.Success::currentlyChosenOptionInDialog,
          ).isEqualTo(fakeTravelOfferTwoOptions.addonOptions[0])
        }
    }
  }

  @Test
  fun `if choose option in dialog show it in the dialog ui but do not change currently chosen until select button is not clicked`() =
    runTest {
      val useCase = FakeGetTravelAddonOfferUseCase()
      val presenter = CustomizeTravelAddonPresenter(
        getTravelAddonOfferUseCase = useCase,
        insuranceId = insuranceId,
      )
      presenter.test(
        CustomizeTravelAddonState.Loading,
      ) {
        useCase.turbine.add(fakeTravelOfferTwoOptions.right())
        skipItems(2)
        sendEvent(CustomizeTravelAddonEvent.ChooseOptionInDialog(fakeTravelAddonQuote2))
        assertThat(awaitItem()).isInstanceOf(CustomizeTravelAddonState.Success::class)
          .apply {
            prop(CustomizeTravelAddonState.Success::currentlyChosenOption).isEqualTo(fakeTravelAddonQuote1)
            prop(CustomizeTravelAddonState.Success::currentlyChosenOptionInDialog).isEqualTo(fakeTravelAddonQuote2)
          }
        sendEvent(CustomizeTravelAddonEvent.ChooseSelectedOption)
        assertThat(awaitItem()).isInstanceOf(CustomizeTravelAddonState.Success::class)
          .apply {
            prop(CustomizeTravelAddonState.Success::currentlyChosenOption).isEqualTo(fakeTravelAddonQuote2)
            prop(CustomizeTravelAddonState.Success::currentlyChosenOptionInDialog).isEqualTo(fakeTravelAddonQuote2)
          }
      }
    }

  @Test
  fun `if reload trigger new data load`() = runTest {
    val useCase = FakeGetTravelAddonOfferUseCase()
    val presenter = CustomizeTravelAddonPresenter(
      getTravelAddonOfferUseCase = useCase,
      insuranceId = insuranceId,
    )
    presenter.test(
      CustomizeTravelAddonState.Loading,
    ) {
      skipItems(1)
      useCase.turbine.add(ErrorMessage().left())
      assertThat(awaitItem()).isInstanceOf(CustomizeTravelAddonState.Failure::class)
      sendEvent(CustomizeTravelAddonEvent.Reload)
      useCase.turbine.add(fakeTravelOfferTwoOptions.right())
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf(CustomizeTravelAddonState.Success::class)
    }
  }
}

private class FakeGetTravelAddonOfferUseCase() : GetTravelAddonOfferUseCase {
  val turbine = Turbine<Either<ErrorMessage, TravelAddonOffer>>()

  override suspend fun invoke(id: String): Either<ErrorMessage, TravelAddonOffer> {
    return turbine.awaitItem()
  }
}

private val fakeTravelAddonQuote1 = TravelAddonQuote(
  quoteId = "id",
  addonId = "addonId1",
  displayName = "45 days",
  displayDetails = listOf(),
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
    49.0,
    UiCurrencyCode.SEK,
  ),
)
private val fakeTravelAddonQuote2 = TravelAddonQuote(
  displayName = "60 days",
  addonId = "addonId1",
  quoteId = "id",
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
  displayDetails = listOf(),
  price = UiMoney(
    60.0,
    UiCurrencyCode.SEK,
  ),
)
private val fakeTravelOfferOnlyOneOption = TravelAddonOffer(
  addonOptions = nonEmptyListOf(
    fakeTravelAddonQuote1,
  ),
  title = "Travel Plus",
  description = "For those who travel often: luggage protection and 24/7 assistance worldwide",
  activationDate = LocalDate(2024, 12, 30),
  currentTravelAddon = null,
)

private val fakeTravelOfferTwoOptions = TravelAddonOffer(
  addonOptions = nonEmptyListOf(
    fakeTravelAddonQuote1,
    fakeTravelAddonQuote2,
  ),
  title = "Travel Plus",
  description = "For those who travel often: luggage protection and 24/7 assistance worldwide",
  activationDate = LocalDate(2024, 12, 30),
  currentTravelAddon = null,
)
