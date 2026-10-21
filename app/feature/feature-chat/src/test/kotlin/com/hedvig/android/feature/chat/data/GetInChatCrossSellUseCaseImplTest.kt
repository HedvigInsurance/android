package com.hedvig.android.feature.chat.data

import assertk.assertThat
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.testing.registerTestResponse
import com.benasher44.uuid.uuid4
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.experiments.Experiment
import com.hedvig.android.experiments.test.FakeExperimentProvider
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.InChatCrossSellEligibilityQuery
import octopus.InChatCrossSellQuery
import octopus.builder.Data
import octopus.builder.buildConversation
import octopus.type.CrossSellExperimentInput
import octopus.type.CrossSellInput
import octopus.type.UserFlow
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetInChatCrossSellUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  private val experimentProvider = FakeExperimentProvider(listOf(Experiment("ACCIDENT_DISCOUNT", "NO_DISCOUNT")))
  private val sentExperiments = listOf(CrossSellExperimentInput(name = "ACCIDENT_DISCOUNT", variant = "NO_DISCOUNT"))

  @Test
  fun `the member's experiments are sent with the in-chat cross-sell query`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient
    val conversationId = uuid4()
    apolloClient.registerTestResponse(
      InChatCrossSellEligibilityQuery(conversationId.toString()),
      InChatCrossSellEligibilityQuery.Data(OctopusFakeResolver) {
        conversation = buildConversation { showCrossSales = true }
      },
    )
    // Registered only for these variables, so a query sent without the member's experiments finds no response.
    apolloClient.registerTestResponse(
      InChatCrossSellQuery(
        CrossSellInput(
          userFlow = UserFlow.IN_CHAT_X_SELL,
          flowSource = Optional.absent(),
          experiments = sentExperiments,
          contractId = Optional.absent(),
          claimId = Optional.absent(),
        ),
      ),
      InChatCrossSellQuery.Data(OctopusFakeResolver),
    )

    assertThat(GetInChatCrossSellUseCaseImpl(apolloClient, experimentProvider).invoke(conversationId)).isRight()
  }
}
