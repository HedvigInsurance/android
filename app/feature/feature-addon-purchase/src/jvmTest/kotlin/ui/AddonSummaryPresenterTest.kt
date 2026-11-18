package ui

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isSameInstanceAs
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.tracking.EventTrackingClient
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
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryEvent.Reload
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryEvent.Submit
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryPresenter
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Content
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Loading
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Purchased
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Submitting
import com.hedvig.android.feature.addon.purchase.ui.summary.CostBreakdownWithExtras
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.public.NavigationRequest
import com.hedvig.android.molecule.test.FrameTestContext
import com.hedvig.android.molecule.test.test
import com.hedvig.android.molecule.test.testFrames
import com.hedvig.android.ui.tiersandaddons.QuoteCostBreakdown
import kotlinx.coroutines.channels.Channel
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
  fun `if receive error offer the failure screen`() = runTest {
    val scheduler = testScheduler
    val submitUseCase = FakeSubmitAddonPurchaseUseCase()
    val insuranceUseCase = FakeAddonSummaryGetInsuranceUseCase()
    val costBreakdownUseCase = FakeGetQuoteCostBreakdownUseCase()
    val eventTrackingClient = FakeEventTrackingClient()
    val presenter = AddonSummaryPresenter(
      summaryParameters = testSummaryParametersWithCurrentAddon,
      submitAddonPurchaseUseCase = submitUseCase,
      addonPurchaseSource = AddonBannerSource.INSURANCES_TAB,
      getQuoteCostBreakdownUseCase = costBreakdownUseCase,
      getInsuranceForTravelAddonUseCase = insuranceUseCase,
      eventTrackingClient = eventTrackingClient,
    )
    presenter.test(AddonSummaryState.Loading) {
      skipItems(1)
      insuranceUseCase.responses.trySend(listOf(fakeInsuranceForAddon).right())
      costBreakdownUseCase.responses.trySend(fakeQuoteCostBreakdown.right())
      skipItems(1)
      sendEvent(AddonSummaryEvent.Submit)
      submitUseCase.responses.trySend(ErrorMessage().left())
      scheduler.advanceUntilIdle()
      assertThat(expectMostRecentItem()).isInstanceOf(AddonSummaryState.Content::class)
        .prop(AddonSummaryState.Content::submitFailure)
        .isNotNull()
        .prop(NavigationRequest<Unit>::isPending)
        .isTrue()
      assertThat(eventTrackingClient.trackedEvents).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `if receive no errors offer the success screen with activationDate from previous parameters`() = runTest {
    val scheduler = testScheduler
    val submitUseCase = FakeSubmitAddonPurchaseUseCase()
    val insuranceUseCase = FakeAddonSummaryGetInsuranceUseCase()
    val costBreakdownUseCase = FakeGetQuoteCostBreakdownUseCase()
    val eventTrackingClient = FakeEventTrackingClient()
    val presenter = AddonSummaryPresenter(
      summaryParameters = testSummaryParametersWithCurrentAddon,
      submitAddonPurchaseUseCase = submitUseCase,
      addonPurchaseSource = AddonBannerSource.INSURANCES_TAB,
      getQuoteCostBreakdownUseCase = costBreakdownUseCase,
      getInsuranceForTravelAddonUseCase = insuranceUseCase,
      eventTrackingClient = eventTrackingClient,
    )
    presenter.test(AddonSummaryState.Loading) {
      skipItems(1)
      insuranceUseCase.responses.trySend(listOf(fakeInsuranceForAddon).right())
      costBreakdownUseCase.responses.trySend(fakeQuoteCostBreakdown.right())
      skipItems(1)
      sendEvent(AddonSummaryEvent.Submit)
      submitUseCase.responses.trySend(Unit.right())
      scheduler.advanceUntilIdle()
      assertThat(expectMostRecentItem()).isInstanceOf(AddonSummaryState.Purchased::class)
        .prop(AddonSummaryState.Purchased::exit)
        .prop(NavigationRequest<LocalDate>::target)
        .isEqualTo(testSummaryParametersWithCurrentAddon.activationDate)
      assertThat(eventTrackingClient.trackedEvents).containsExactly(
        "addon_purchased" to mapOf(
          "user_flow" to "insurance_screen",
          "addon_type" to newQuote.addonVariant.product,
          "contract_id" to "contractId",
          "price" to 59.0,
          "currency" to "SEK",
          "quote_id" to "quoteId",
          "purchase_type" to "upgrade",
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `buying several toggleable addons next to an active one tracks one new purchase per addon`() = runTest {
    val scheduler = testScheduler
    val submitUseCase = FakeSubmitAddonPurchaseUseCase()
    val insuranceUseCase = FakeAddonSummaryGetInsuranceUseCase()
    val costBreakdownUseCase = FakeGetQuoteCostBreakdownUseCase()
    val eventTrackingClient = FakeEventTrackingClient()
    val presenter = AddonSummaryPresenter(
      summaryParameters = testSummaryParametersToggleableCarAddons,
      submitAddonPurchaseUseCase = submitUseCase,
      addonPurchaseSource = AddonBannerSource.HOME_CROSS_SELL_SHEET,
      getQuoteCostBreakdownUseCase = costBreakdownUseCase,
      getInsuranceForTravelAddonUseCase = insuranceUseCase,
      eventTrackingClient = eventTrackingClient,
    )
    presenter.test(AddonSummaryState.Loading) {
      skipItems(1)
      insuranceUseCase.responses.trySend(listOf(fakeInsuranceForAddon).right())
      costBreakdownUseCase.responses.trySend(fakeQuoteCostBreakdown.right())
      skipItems(1)
      sendEvent(AddonSummaryEvent.Submit)
      submitUseCase.responses.trySend(Unit.right())
      scheduler.advanceUntilIdle()
      assertThat(eventTrackingClient.trackedEvents).containsExactly(
        "addon_purchased" to mapOf(
          "user_flow" to "home",
          "addon_type" to "SE_CAR_ALLRISK_ADDON",
          "contract_id" to "contractId",
          "price" to 99.0,
          "currency" to "SEK",
          "quote_id" to "carQuoteId",
          "purchase_type" to "new",
        ),
        "addon_purchased" to mapOf(
          "user_flow" to "home",
          "addon_type" to "SE_CAR_RENTAL_CAR_ADDON",
          "contract_id" to "contractId",
          "price" to 39.0,
          "currency" to "SEK",
          "quote_id" to "carQuoteId",
          "purchase_type" to "new",
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a first selectable addon bought from contract detail is a new purchase from insurance_card`() = runTest {
    val scheduler = testScheduler
    val submitUseCase = FakeSubmitAddonPurchaseUseCase()
    val insuranceUseCase = FakeAddonSummaryGetInsuranceUseCase()
    val costBreakdownUseCase = FakeGetQuoteCostBreakdownUseCase()
    val eventTrackingClient = FakeEventTrackingClient()
    val presenter = AddonSummaryPresenter(
      summaryParameters = testSummaryParametersNoCurrentAddon,
      submitAddonPurchaseUseCase = submitUseCase,
      addonPurchaseSource = AddonBannerSource.CONTRACT_DETAIL,
      getQuoteCostBreakdownUseCase = costBreakdownUseCase,
      getInsuranceForTravelAddonUseCase = insuranceUseCase,
      eventTrackingClient = eventTrackingClient,
    )
    presenter.test(AddonSummaryState.Loading) {
      skipItems(1)
      insuranceUseCase.responses.trySend(listOf(fakeInsuranceForAddon).right())
      costBreakdownUseCase.responses.trySend(fakeQuoteCostBreakdown.right())
      skipItems(1)
      sendEvent(AddonSummaryEvent.Submit)
      submitUseCase.responses.trySend(Unit.right())
      scheduler.advanceUntilIdle()
      assertThat(eventTrackingClient.trackedEvents).containsExactly(
        "addon_purchased" to mapOf(
          "user_flow" to "insurance_card",
          "addon_type" to newQuote.addonVariant.product,
          "contract_id" to "contractId",
          "price" to 59.0,
          "currency" to "SEK",
          "quote_id" to "quoteId",
          "purchase_type" to "new",
        ),
      )
      cancelAndIgnoreRemainingEvents()
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
      eventTrackingClient = FakeEventTrackingClient(),
    )
    presenter1.test(AddonSummaryState.Loading) {
      skipItems(1)
      insuranceUseCase1.responses.trySend(listOf(fakeInsuranceForAddon).right())
      costBreakdownUseCase1.responses.trySend(fakeQuoteCostBreakdown.right())
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
      eventTrackingClient = FakeEventTrackingClient(),
    )
    presenter2.test(AddonSummaryState.Loading) {
      skipItems(1)
      insuranceUseCase2.responses.trySend(listOf(fakeInsuranceForAddon).right())
      costBreakdownUseCase2.responses.trySend(fakeQuoteCostBreakdown.right())
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
      eventTrackingClient = FakeEventTrackingClient(),
    )
    presenter.test(AddonSummaryState.Loading) {
      skipItems(1)
      insuranceUseCase.responses.trySend(listOf(fakeInsuranceForAddon).right())
      costBreakdownUseCase.responses.trySend(fakeQuoteCostBreakdown.right())
      assertThat(awaitItem()).isInstanceOf(AddonSummaryState.Content::class)
        .prop(AddonSummaryState.Content::costBreakdownWithExtras)
        .prop(CostBreakdownWithExtras::totalExtra)
        .isEqualTo(newQuote.itemCost.monthlyNet)
    }
  }

  @Test
  fun `a purchase ends in Purchased, offering the success screen for the activation date once`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      loadedContent(fixture)
      send(Submit)
      assertThat(latest).isEqualTo(Submitting)
      assertThat(fixture.submit.calls).containsExactly("quoteId" to listOf("addonId1"))

      fixture.submit.responses.trySend(Unit.right())
      frame()
      val purchased = latest as Purchased
      assertThat(purchased.exit.target).isEqualTo(fixture.params.activationDate)
      assertThat(purchased.exit.tryConsume()).isTrue()
      assertThat(purchased.exit.tryConsume()).isFalse()
      assertThat(fixture.tracking.trackedEvents.map { it.first }).containsExactly("addon_purchased")
    }
  }

  @Test
  fun `a failed purchase shows the summary again and offers the failure screen over it`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      val content = loadedContent(fixture)
      send(Submit)
      fixture.submit.responses.trySend(ErrorMessage().left())
      frame()
      val failed = latest as Content
      assertThat(failed.copy(submitFailure = null)).isEqualTo(content)
      assertThat(failed.submitFailure).isNotNull().prop(NavigationRequest<Unit>::isPending).isTrue()
      assertThat(fixture.tracking.trackedEvents).isEmpty()
    }
  }

  @Test
  fun `a retry after the failure screen was shown buys and offers the success screen`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      loadedContent(fixture)
      send(Submit)
      fixture.submit.responses.trySend(ErrorMessage().left())
      frame()
      assertThat((latest as Content).submitFailure?.tryConsume()).isEqualTo(true)

      send(Submit)
      assertThat(latest).isEqualTo(Submitting)
      fixture.submit.responses.trySend(ErrorMessage().left())
      frame()
      val secondFailure = (latest as Content).submitFailure
      assertThat(secondFailure).isNotNull().prop(NavigationRequest<Unit>::isPending).isTrue()
      assertThat(secondFailure!!.tryConsume()).isTrue()

      send(Submit)
      fixture.submit.responses.trySend(Unit.right())
      frame()
      assertThat(fixture.submit.calls).hasSize(3)
      assertThat(latest).isInstanceOf(Purchased::class)
    }
  }

  @Test
  fun `two submits in one frame buy once`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      loadedContent(fixture)
      send(Submit, Submit)
      fixture.submit.responses.trySend(Unit.right())
      frame()
      assertThat(fixture.submit.calls).hasSize(1)
      assertThat(latest).isInstanceOf(Purchased::class)
    }
  }

  @Test
  fun `a submit while a purchase is in flight is ignored and the purchase completes`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      loadedContent(fixture)
      send(Submit)
      send(Submit)
      assertThat(latest).isEqualTo(Submitting)
      fixture.submit.responses.trySend(Unit.right())
      frame()
      assertThat(fixture.submit.calls).hasSize(1)
      assertThat(latest).isInstanceOf(Purchased::class)
      assertThat(fixture.tracking.trackedEvents).hasSize(1)
    }
  }

  @Test
  fun `a submit handled in the frame a failure lands is ignored until the failure screen is shown`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      loadedContent(fixture)
      send(Submit)
      fixture.submit.responses.trySend(ErrorMessage().left())
      send(Submit)
      assertThat(fixture.submit.calls).hasSize(1)
      val failure = (latest as Content).submitFailure
      assertThat(failure).isNotNull().prop(NavigationRequest<Unit>::isPending).isTrue()

      failure!!.tryConsume()
      send(Submit)
      assertThat(fixture.submit.calls).hasSize(2)
      assertThat(latest).isEqualTo(Submitting)
    }
  }

  @Test
  fun `a submit before the summary has loaded is ignored`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      send(Submit)
      val content = loadedContent(fixture)
      assertThat(latest).isEqualTo(content)
      assertThat(fixture.submit.calls).isEmpty()

      send(Submit)
      assertThat(fixture.submit.calls).hasSize(1)
    }
  }

  @Test
  fun `a failed load shows the error, and reload shows the summary`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      fixture.insurance.responses.trySend(emptyList<InsuranceForAddon>().right())
      frame()
      frame()
      assertThat(latest).isEqualTo(AddonSummaryState.Error)

      send(Reload)
      loadedContent(fixture)
      assertThat(fixture.insurance.calls).isEqualTo(2)
    }
  }

  @Test
  fun `a restart while loading loads again`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      frame()
      restart()
      frame()
      assertThat(latest).isEqualTo(Loading)
      loadedContent(fixture)
      assertThat(fixture.insurance.calls).isEqualTo(2)
    }
  }

  @Test
  fun `a restart from the summary keeps showing it while it loads again`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      val content = loadedContent(fixture)
      restart()
      frame()
      assertThat(latest).isEqualTo(content)
      assertThat(loadedContent(fixture)).isEqualTo(content)
      assertThat(fixture.insurance.calls).isEqualTo(2)
    }
  }

  /** The purchase stops with the presenter and may have reached the server; the member decides whether to buy again. */
  @Test
  fun `a restart while purchasing shows the summary again without an outcome, and allows a second purchase`() =
    runTest {
      val fixture = Fixture()
      fixture.presenter.testFrames(Loading) {
        val content = loadedContent(fixture)
        send(Submit)
        restart()
        frame()
        assertThat(latest).isEqualTo(Loading)
        assertThat(loadedContent(fixture)).isEqualTo(content)

        send(Submit)
        assertThat(fixture.submit.calls).hasSize(2)
        assertThat(latest).isEqualTo(Submitting)
      }
    }

  @Test
  fun `a restart after the failure screen was shown does not offer it again`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      val content = loadedContent(fixture)
      send(Submit)
      fixture.submit.responses.trySend(ErrorMessage().left())
      frame()
      val failure = (latest as Content).submitFailure!!
      assertThat(failure.tryConsume()).isTrue()

      restart()
      frame()
      assertThat(loadedContent(fixture).copy(submitFailure = null)).isEqualTo(content)
      assertThat((latest as Content).submitFailure).isEqualTo(failure)
      assertThat(failure.tryConsume()).isFalse()
    }
  }

  /** The member did not send the purchase again, so the failure is still theirs to see, as when the presenter ran on. */
  @Test
  fun `a restart before the failure screen was shown still offers it once`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      loadedContent(fixture)
      send(Submit)
      fixture.submit.responses.trySend(ErrorMessage().left())
      frame()
      val failure = (latest as Content).submitFailure!!

      restart()
      loadedContent(fixture)
      assertThat((latest as Content).submitFailure).isEqualTo(failure)
      assertThat(failure.tryConsume()).isTrue()
      assertThat(fixture.submit.calls).hasSize(1)
    }
  }

  @Test
  fun `a restart after a purchase keeps the outcome without loading or buying again`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      loadedContent(fixture)
      send(Submit)
      fixture.submit.responses.trySend(Unit.right())
      frame()
      val purchased = latest as Purchased
      assertThat(purchased.exit.tryConsume()).isTrue()

      restart()
      frame()
      fixture.answerLoad()
      send(Submit)
      assertThat(latest).isSameInstanceAs(purchased)
      assertThat(purchased.exit.tryConsume()).isFalse()
      assertThat(fixture.insurance.calls).isEqualTo(1)
      assertThat(fixture.submit.calls).hasSize(1)
    }
  }

  @Test
  fun `a restart before the success screen was shown still offers it once`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      loadedContent(fixture)
      send(Submit)
      fixture.submit.responses.trySend(Unit.right())
      frame()
      val purchased = latest as Purchased

      restart()
      frame()
      assertThat(latest).isSameInstanceAs(purchased)
      assertThat(purchased.exit.tryConsume()).isTrue()
      assertThat(fixture.tracking.trackedEvents).hasSize(1)
    }
  }

  @Test
  fun `every event after a purchase is ignored`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      loadedContent(fixture)
      send(Submit)
      fixture.submit.responses.trySend(Unit.right())
      frame()
      val purchased = latest as Purchased

      send(Reload)
      fixture.answerLoad()
      send(Submit)
      frame()
      assertThat(latest).isSameInstanceAs(purchased)
      assertThat(observed.last()).isSameInstanceAs(purchased)
      assertThat(fixture.insurance.calls).isEqualTo(1)
      assertThat(fixture.submit.calls).hasSize(1)
    }
  }

  @Test
  fun `reload while purchasing keeps showing the purchase in flight`() = runTest {
    val fixture = Fixture()
    fixture.presenter.testFrames(Loading) {
      loadedContent(fixture)
      send(Submit)
      send(Reload)
      fixture.answerLoad()
      frame()
      frame()
      assertThat(latest).isEqualTo(Submitting)
      assertThat(fixture.submit.calls).hasSize(1)
    }
  }
}

private class FakeEventTrackingClient : EventTrackingClient {
  val trackedEvents = mutableListOf<Pair<String, Map<String, Any?>>>()

  override fun setCollectionEnabled(enabled: Boolean) {}

  override fun trackEvent(name: String, parameters: Map<String, Any?>) {
    trackedEvents += name to parameters
  }

  override fun trackScreen(name: String, screenClass: String?, parameters: Map<String, Any?>) {}

  override fun setUserId(userId: String?) {}

  override fun setUserProperty(name: String, value: String?) {}
}

/** Each purchase waits for one entry in [responses]; [calls] records every purchase sent, also one cancelled later. */
private class FakeSubmitAddonPurchaseUseCase : SubmitAddonPurchaseUseCase {
  val responses = Channel<Either<ErrorMessage, Unit>>(Channel.UNLIMITED)
  val calls = mutableListOf<Pair<String, List<String>>>()

  override suspend fun invoke(quoteId: String, addonIds: List<String>): Either<ErrorMessage, Unit> {
    calls += quoteId to addonIds
    return responses.receive()
  }
}

private class FakeAddonSummaryGetInsuranceUseCase : GetInsuranceForTravelAddonUseCase {
  val responses = Channel<Either<ErrorMessage, List<InsuranceForAddon>>>(Channel.UNLIMITED)
  var calls = 0

  override suspend fun invoke(ids: List<String>): Flow<Either<ErrorMessage, List<InsuranceForAddon>>> {
    calls++
    return flow { emit(responses.receive()) }
  }
}

private class FakeGetQuoteCostBreakdownUseCase : GetQuoteCostBreakdownUseCase {
  val responses = Channel<Either<ErrorMessage, QuoteCostBreakdown>>(Channel.UNLIMITED)

  override suspend fun invoke(
    quoteId: String,
    existingAddons: List<CurrentlyActiveAddon>,
    newAddons: List<AddonQuote>,
    baseCost: ItemCost,
    insuranceDisplayName: String,
    addonType: AddonType,
  ): Either<ErrorMessage, QuoteCostBreakdown> {
    return responses.receive()
  }
}

/** The summary's presenter over fakes. */
private class Fixture(val params: SummaryParameters = testSummaryParametersWithCurrentAddon) {
  val submit = FakeSubmitAddonPurchaseUseCase()
  val insurance = FakeAddonSummaryGetInsuranceUseCase()
  val costBreakdown = FakeGetQuoteCostBreakdownUseCase()
  val tracking = FakeEventTrackingClient()
  val presenter = AddonSummaryPresenter(
    summaryParameters = params,
    submitAddonPurchaseUseCase = submit,
    addonPurchaseSource = AddonBannerSource.INSURANCES_TAB,
    getQuoteCostBreakdownUseCase = costBreakdown,
    getInsuranceForTravelAddonUseCase = insurance,
    eventTrackingClient = tracking,
  )

  fun answerLoad() {
    insurance.responses.trySend(listOf(fakeInsuranceForAddon).right())
    costBreakdown.responses.trySend(fakeQuoteCostBreakdown.right())
  }
}

/** Answers the load and returns the summary it shows. */
private fun FrameTestContext<AddonSummaryEvent, AddonSummaryState>.loadedContent(fixture: Fixture): Content {
  fixture.answerLoad()
  frame()
  frame()
  return latest as Content
}

/** Sends [events] together and lets their effects run and show their state. */
private fun FrameTestContext<AddonSummaryEvent, AddonSummaryState>.send(vararg events: AddonSummaryEvent) {
  emit(*events)
  frame()
  frame()
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

private fun carAddonQuote(addonId: String, product: String, monthlyPrice: Double) = AddonQuote(
  displayTitle = product,
  addonId = addonId,
  displayDetails = emptyList(),
  addonVariant = AddonVariant(
    termsVersion = "terms",
    displayName = product,
    product = product,
    perils = listOf(),
    documents = listOf(),
  ),
  addonSubtype = null,
  documents = listOf(),
  displayDescription = product,
  itemCost = ItemCost(
    monthlyGross = UiMoney(monthlyPrice, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(monthlyPrice, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
)

private val testSummaryParametersToggleableCarAddons = SummaryParameters(
  productVariant = fakeProductVariant,
  contractId = "contractId",
  baseInsuranceCost = ItemCost(
    monthlyGross = UiMoney(100.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(100.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
  chosenQuotes = listOf(
    carAddonQuote("allRiskId", "SE_CAR_ALLRISK_ADDON", 99.0),
    carAddonQuote("rentalCarId", "SE_CAR_RENTAL_CAR_ADDON", 39.0),
  ),
  activationDate = LocalDate(2024, 12, 30),
  currentlyActiveAddons = listOf(currentAddon),
  quoteId = "carQuoteId",
  notificationMessage = null,
  addonType = AddonType.TOGGLEABLE,
)
