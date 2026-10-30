package com.hedvig.android.crosssells

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.core.tracking.test.FakeEventTrackingClient
import org.junit.Test

class CrossSellTrackingTest {
  private val redirectUrl =
    "https://www.hedvig.com/api/redirect/recommend?attributed_to=HEDVIG&initiated_from=APP" +
      "&attribution.userFlow=HOME&attribution.experiments.ACCIDENT_DISCOUNT=DISCOUNT_50" +
      "&next=%2Fse-en%2Finsurances%2Faccident&code=accident50app&requiresAuthorization=true"

  @Test
  fun `the event carries the offer id, the campaign code and the experiment group instead of the url`() {
    val client = FakeEventTrackingClient()

    client.trackCrossSellClicked(CrossSellUserFlow.HOME, "crossSell:ACCIDENT", redirectUrl)

    assertThat(client.events).containsExactly(
      FakeEventTrackingClient.TrackedEvent(
        name = "cross_sell_clicked",
        parameters = mapOf(
          "user_flow" to "home",
          "offer_id" to "crossSell:ACCIDENT",
          "campaign_code" to "accident50app",
          "ACCIDENT_DISCOUNT" to "DISCOUNT_50",
        ),
      ),
    )
  }

  @Test
  fun `shown, clicked and dismissed describe the offer the same way`() {
    val client = FakeEventTrackingClient()

    client.trackCrossSellShown(CrossSellUserFlow.IN_CHAT_X_SELL, "crossSell:ALL", redirectUrl)
    client.trackCrossSellClicked(CrossSellUserFlow.IN_CHAT_X_SELL, "crossSell:ALL", redirectUrl)
    client.trackCrossSellDismissed(CrossSellUserFlow.IN_CHAT_X_SELL, "crossSell:ALL", redirectUrl)

    val parameters = mapOf(
      "user_flow" to "in_chat_x_sell",
      "offer_id" to "crossSell:ALL",
      "campaign_code" to "accident50app",
      "ACCIDENT_DISCOUNT" to "DISCOUNT_50",
    )
    assertThat(client.events).containsExactly(
      FakeEventTrackingClient.TrackedEvent(name = "cross_sell_shown", parameters = parameters),
      FakeEventTrackingClient.TrackedEvent(name = "cross_sell_clicked", parameters = parameters),
      FakeEventTrackingClient.TrackedEvent(name = "cross_sell_dismissed", parameters = parameters),
    )
  }

  @Test
  fun `the shown reporter reports each offer once`() {
    val reported = mutableListOf<String>()
    val reporter = CrossSellShownReporter { offerId, _ -> reported += offerId }

    reporter.report("crossSell:CAR", "url")
    reporter.report("crossSell:HOME", "url")
    reporter.report("crossSell:CAR", "url")

    assertThat(reported).containsExactly("crossSell:CAR", "crossSell:HOME")
  }

  @Test
  fun `a link without a campaign code sends no campaign_code`() {
    val client = FakeEventTrackingClient()
    val url = "https://www.hedvig.com/api/redirect/recommend" +
      "?next=%2Fse%2Fforsakringar%2Fbilforsakring&requiresAuthorization=true"

    client.trackCrossSellClicked(CrossSellUserFlow.INSURANCES, "crossSell:CAR", url)

    assertThat(client.events.single().parameters).isEqualTo(
      mapOf(
        "user_flow" to "insurances",
        "offer_id" to "crossSell:CAR",
      ),
    )
  }

  @Test
  fun `a malformed escape in the campaign code is kept as written while valid ones decode`() {
    assertThat(campaignCode("https://www.hedvig.com/api/redirect/recommend?code=50%25off%")).isEqualTo("50%off%")
  }

  @Test
  fun `a plus in the campaign code stays a plus`() {
    assertThat(campaignCode("https://www.hedvig.com/api/redirect/recommend?code=summer+10")).isEqualTo("summer+10")
  }

  @Test
  fun `escapes decode as UTF-8, including multi-byte characters`() {
    assertThat(percentDecode("r%C3%A4tt%2B50%20off")).isEqualTo("rätt+50 off")
  }

  @Test
  fun `a plain product link has no campaign code`() {
    assertThat(campaignCode("https://www.hedvig.com/se/forsakringar/djurforsakring")).isNull()
  }

  @Test
  fun `every experiment in the link becomes a parameter named after it`() {
    val url = "https://www.hedvig.com/api/redirect/recommend?attribution.userFlow=HOME" +
      "&attribution.experiments.ACCIDENT_DISCOUNT=NO_DISCOUNT&attribution.experiments.RECOMMENDED_CROSS_SELL=B"

    assertThat(experiments(url)).isEqualTo(mapOf("ACCIDENT_DISCOUNT" to "NO_DISCOUNT", "RECOMMENDED_CROSS_SELL" to "B"))
  }

  @Test
  fun `a link without experiments sends no experiment parameter`() {
    val client = FakeEventTrackingClient()
    val url = "https://www.hedvig.com/api/redirect/recommend?attribution.userFlow=HOME&code=accident50app"

    client.trackCrossSellShown(CrossSellUserFlow.HOME, "crossSell:ACCIDENT", url)

    assertThat(client.events.single().parameters).isEqualTo(
      mapOf(
        "user_flow" to "home",
        "offer_id" to "crossSell:ACCIDENT",
        "campaign_code" to "accident50app",
      ),
    )
  }

  @Test
  fun `an experiment Firebase cannot take as a parameter name is left out`() {
    val url = "https://www.hedvig.com/api/redirect/recommend?attribution.experiments.1ST=A" +
      "&attribution.experiments.WITH-DASH=B&attribution.experiments.${"X".repeat(41)}=C"

    assertThat(experiments(url)).isEqualTo(emptyMap())
  }

  @Test
  fun `an experiment never replaces one of the event's own parameters`() {
    val client = FakeEventTrackingClient()
    val url = "https://www.hedvig.com/api/redirect/recommend?attribution.experiments.offer_id=X"

    client.trackCrossSellShown(CrossSellUserFlow.HOME, "crossSell:ACCIDENT", url)

    assertThat(client.events.single().parameters["offer_id"]).isEqualTo("crossSell:ACCIDENT")
  }
}
