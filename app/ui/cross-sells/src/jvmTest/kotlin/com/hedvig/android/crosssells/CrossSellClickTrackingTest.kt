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
}
