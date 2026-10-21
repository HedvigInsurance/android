package com.hedvig.android.crosssells

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.core.tracking.test.FakeEventTrackingClient
import org.junit.Test

class CrossSellClickTrackingTest {
  private val redirectUrl =
    "https://www.hedvig.com/api/redirect/recommend?attributed_to=HEDVIG&initiated_from=APP" +
      "&attribution.userFlow=HOME&attribution.experiments.ACCIDENT_DISCOUNT=CONTROL" +
      "&next=%2Fse-en%2Finsurances%2Faccident&code=accident50app&requiresAuthorization=true"

  @Test
  fun `the event carries the offer id and the campaign code instead of the url`() {
    val client = FakeEventTrackingClient()

    client.trackCrossSellClicked(CrossSellUserFlow.HOME, "crossSell:ACCIDENT", redirectUrl)

    assertThat(client.events).containsExactly(
      FakeEventTrackingClient.TrackedEvent(
        name = "cross_sell_clicked",
        parameters = mapOf(
          "user_flow" to "home",
          "offer_id" to "crossSell:ACCIDENT",
          "campaign_code" to "accident50app",
        ),
      ),
    )
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
  fun `a plain product link has no campaign code`() {
    assertThat(campaignCode("https://www.hedvig.com/se/forsakringar/djurforsakring")).isNull()
  }
}
