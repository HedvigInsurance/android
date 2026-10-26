package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.enqueueTestNetworkError
import com.apollographql.apollo.testing.enqueueTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.CrossSell
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import octopus.CrossSellsQuery
import octopus.builder.Data
import octopus.builder.buildCrossSell
import octopus.builder.buildCrossSellV2
import octopus.builder.buildMember
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetCrossSellsUseCaseImplTest {
  @get:Rule
  val testApolloClientRule = TestApolloClientRule()
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `a cached answer is emitted first and then replaced by the network answer`() = runTest {
    val useCase = GetCrossSellsUseCaseImpl(apolloClient)
    apolloClient.enqueueTestResponse(CrossSellsQuery(), data("accident"))
    apolloClient.enqueueTestResponse(CrossSellsQuery(), data("car"))

    assertThat(useCase.crossSellIdEmissions()).containsExactly(listOf("accident").right())
    assertThat(useCase.crossSellIdEmissions()).containsExactly(listOf("accident").right(), listOf("car").right())
  }

  @Test
  fun `an unreachable network keeps the cached answer`() = runTest {
    val useCase = GetCrossSellsUseCaseImpl(apolloClient)
    apolloClient.enqueueTestResponse(CrossSellsQuery(), data("accident"))
    apolloClient.enqueueTestNetworkError()

    useCase.crossSellIdEmissions()

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
