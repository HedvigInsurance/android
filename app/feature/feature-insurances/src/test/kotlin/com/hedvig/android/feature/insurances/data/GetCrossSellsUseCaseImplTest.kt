package com.hedvig.android.feature.insurances.data

import assertk.assertThat
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.experiments.Experiment
import com.hedvig.android.experiments.test.FakeExperimentProvider
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.CrossSellsQuery
import octopus.builder.Data
import octopus.type.CrossSellExperimentInput
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetCrossSellsUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  private val experimentProvider = FakeExperimentProvider(listOf(Experiment("ACCIDENT_DISCOUNT", "NO_DISCOUNT")))
  private val sentExperiments = listOf(CrossSellExperimentInput(name = "ACCIDENT_DISCOUNT", variant = "NO_DISCOUNT"))

  @Test
  fun `the member's experiments are sent with the cross-sells query`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient
    // Registered only for these variables, so a query sent without the member's experiments finds no response.
    apolloClient.registerTestResponse(CrossSellsQuery(sentExperiments), CrossSellsQuery.Data(OctopusFakeResolver))

    assertThat(GetCrossSellsUseCaseImpl(apolloClient, experimentProvider).invoke()).isRight()
  }
}
