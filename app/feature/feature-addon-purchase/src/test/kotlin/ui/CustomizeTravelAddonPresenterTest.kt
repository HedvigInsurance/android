package ui

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.nonEmptyListOf
import assertk.assertThat
import assertk.assertions.isInstanceOf
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.addon.purchase.data.Addon
import com.hedvig.android.feature.addon.purchase.data.Addon.TravelAddonOffer
import com.hedvig.android.feature.addon.purchase.data.AddonVariant
import com.hedvig.android.feature.addon.purchase.data.GetTravelAddonOfferUseCase
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuote
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
        travelAddonOffer = TravelAddonOffer(
          addonOptions = nonEmptyListOf(fakeTravelAddonQuote1, fakeTravelAddonQuote2),
          title = "",
          description = "",
          activationDate = LocalDate(2025, 1, 1),
          currentTravelAddon = null,
        ),
        currentlyChosenOption = fakeTravelAddonQuote1,
        currentlyChosenOptionInDialog = fakeTravelAddonQuote1,
      ),
    ) {
      skipItems(1)
      useCase.turbine.add(ErrorMessage().left())
      expectNoEvents()
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
  addonVariant = AddonVariant(
    termsVersion = "terms",
    documents = listOf(),
    displayDetails = listOf(),
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
    documents = listOf(),
    displayDetails = listOf(),
  ),
  price = UiMoney(
    60.0,
    UiCurrencyCode.SEK,
  ),
)
private val fakeTravelAddon = TravelAddonOffer(
  addonOptions = nonEmptyListOf(
    fakeTravelAddonQuote1,
    fakeTravelAddonQuote2,
  ),
  title = "Travel Plus",
  description = "For those who travel often: luggage protection and 24/7 assistance worldwide",
  activationDate = LocalDate(2024, 12, 30),
  currentTravelAddon = null,
)
