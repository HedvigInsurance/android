package com.hedvig.android.feature.cross.sell.sheet

import assertk.assertThat
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.experiments.Experiment
import com.hedvig.android.experiments.test.FakeExperimentProvider
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import octopus.BottomSheetCrossSellsQuery
import octopus.builder.Data
import octopus.type.CrossSellExperimentInput
import octopus.type.CrossSellInput
import octopus.type.UserFlow
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetCrossSellSheetDataUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  private val experimentProvider = FakeExperimentProvider(listOf(Experiment("ACCIDENT_DISCOUNT", "NO_DISCOUNT")))
  private val sentExperiments = listOf(CrossSellExperimentInput(name = "ACCIDENT_DISCOUNT", variant = "NO_DISCOUNT"))

  @Test
  fun `the member's experiments are sent with the cross-sell sheet query`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient
    val source = CrossSellInput(
      userFlow = UserFlow.SMART_X_SELL,
      flowSource = Optional.absent(),
      experiments = emptyList(),
      contractId = Optional.absent(),
      claimId = Optional.absent(),
    )
    // Registered only for these variables, so a query sent without the member's experiments finds no response.
    apolloClient.registerTestResponse(
      BottomSheetCrossSellsQuery(source.copy(experiments = sentExperiments)),
      BottomSheetCrossSellsQuery.Data(OctopusFakeResolver),
    )

    assertThat(GetCrossSellSheetDataUseCaseImpl(apolloClient, experimentProvider).invoke(source).first()).isRight()
  }
}
