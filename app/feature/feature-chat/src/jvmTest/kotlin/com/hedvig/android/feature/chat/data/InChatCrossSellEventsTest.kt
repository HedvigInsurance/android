package com.hedvig.android.feature.chat.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.tracking.test.FakeEventTrackingClient
import com.hedvig.android.core.tracking.test.FakeEventTrackingClient.TrackedEvent
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class InChatCrossSellEventsTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private class FakeHideInChatCrossSellUseCase(
    private val result: Either<ErrorMessage, Unit> = Unit.right(),
  ) : HideInChatCrossSellUseCase {
    val hiddenConversations = mutableListOf<Uuid>()

    override suspend fun invoke(conversationId: Uuid): Either<ErrorMessage, Unit> {
      hiddenConversations += conversationId
      return result
    }
  }

  private val conversationId = uuid4()
  private val offerUrl = "https://www.hedvig.com/api/redirect/recommend?code=accident50app&requiresAuthorization=true"

  private fun expectedEvent(name: String) = TrackedEvent(
    name = name,
    parameters = mapOf(
      "user_flow" to "in_chat_x_sell",
      "offer_id" to "crossSell:ALL",
      "campaign_code" to "accident50app",
    ),
  )

  @Test
  fun `the card being on screen reports cross_sell_shown and leaves the offer in place`() = runTest {
    val client = FakeEventTrackingClient()
    val hide = FakeHideInChatCrossSellUseCase()

    InChatCrossSellEvents(conversationId, client, hide).shown("crossSell:ALL", offerUrl)

    assertThat(client.events).containsExactly(expectedEvent("cross_sell_shown"))
    assertThat(hide.hiddenConversations).isEmpty()
  }

  @Test
  fun `turning the offer down reports cross_sell_dismissed and ends it for the conversation`() = runTest {
    val client = FakeEventTrackingClient()
    val hide = FakeHideInChatCrossSellUseCase()

    InChatCrossSellEvents(conversationId, client, hide).dismissed("crossSell:ALL", offerUrl)

    assertThat(client.events).containsExactly(expectedEvent("cross_sell_dismissed"))
    assertThat(hide.hiddenConversations).containsExactly(conversationId)
  }

  @Test
  fun `taking the offer reports cross_sell_clicked and ends it for the conversation`() = runTest {
    val client = FakeEventTrackingClient()
    val hide = FakeHideInChatCrossSellUseCase()

    InChatCrossSellEvents(conversationId, client, hide).clicked("crossSell:ALL", offerUrl)

    assertThat(client.events).containsExactly(expectedEvent("cross_sell_clicked"))
    assertThat(hide.hiddenConversations).containsExactly(conversationId)
  }

  @Test
  fun `the event is reported even when ending the offer fails`() = runTest {
    val client = FakeEventTrackingClient()
    val hide = FakeHideInChatCrossSellUseCase(ErrorMessage("offline").left())

    InChatCrossSellEvents(conversationId, client, hide).dismissed("crossSell:ALL", offerUrl)

    assertThat(client.events).containsExactly(expectedEvent("cross_sell_dismissed"))
  }
}
