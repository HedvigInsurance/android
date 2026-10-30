package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestNetworkError
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.experiments.Experiment
import com.hedvig.android.experiments.test.FakeExperimentProvider
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import octopus.CrossSellsQuery
import octopus.builder.Data
import octopus.builder.buildCrossSell
import octopus.builder.buildCrossSellV2
import octopus.builder.buildMember
import octopus.type.CrossSellExperimentInput
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetCrossSellsUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  private val experimentProvider = FakeExperimentProvider(listOf(Experiment("ACCIDENT_DISCOUNT", "NO_DISCOUNT")))
  private val sentExperiments = listOf(CrossSellExperimentInput(name = "ACCIDENT_DISCOUNT", variant = "NO_DISCOUNT"))
  private val query = CrossSellsQuery(sentExperiments)

  @Test
  fun `the member's experiments are sent with the cross-sells query`() = runTest {
    // Registered only for these variables, so a query sent without the member's experiments finds no response.
    apolloClient.registerTestResponse(query, CrossSellsQuery.Data(OctopusFakeResolver))

    assertThat(GetCrossSellsUseCaseImpl(apolloClient, experimentProvider).invoke().first()).isRight()
  }

  @Test
  fun `a cached answer is emitted first and then replaced by the network answer`() = runTest {
    val useCase = GetCrossSellsUseCaseImpl(apolloClient, experimentProvider)
    apolloClient.registerTestResponse(query, data("accident"))
    assertThat(useCase.crossSellIdEmissions()).containsExactly(listOf("accident").right())

    apolloClient.registerTestResponse(query, data("car"))
    assertThat(useCase.crossSellIdEmissions()).containsExactly(listOf("accident").right(), listOf("car").right())
  }

  @Test
  fun `an unreachable network keeps the cached answer`() = runTest {
    val useCase = GetCrossSellsUseCaseImpl(apolloClient, experimentProvider)
    apolloClient.registerTestResponse(query, data("accident"))
    useCase.crossSellIdEmissions()

    apolloClient.registerTestNetworkError(query)
    assertThat(useCase.crossSellIdEmissions()).containsExactly(listOf("accident").right())
  }

  private suspend fun GetCrossSellsUseCaseImpl.crossSellIdEmissions(): List<Either<ErrorMessage, List<String>>> {
    return invoke().toList().map { result -> result.map { crossSells -> crossSells.map(CrossSell::id) } }
  }

  private fun data(vararg crossSellIds: String) = CrossSellsQuery.Data(OctopusFakeResolver) {
    currentMember = buildMember {
      crossSellV2 = buildCrossSellV2 {
        otherCrossSells = crossSellIds.map { crossSellId -> buildCrossSell { id = crossSellId } }
      }
    }
  }
}
