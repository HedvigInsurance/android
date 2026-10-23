package com.hedvig.android.feature.insurances.data

import assertk.assertThat
import assertk.assertions.containsExactly
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.QueueTestNetworkTransport
import com.apollographql.apollo.testing.enqueueTestNetworkError
import com.apollographql.apollo.testing.enqueueTestResponse
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.data.contract.CrossSell
import kotlinx.coroutines.test.runTest
import octopus.CrossSellsQuery
import octopus.builder.Data
import octopus.builder.buildCrossSell
import octopus.builder.buildCrossSellV2
import octopus.builder.buildMember
import octopus.cache.Cache
import org.junit.After
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetCrossSellsUseCaseImplTest {
  private val apolloClient = with(Cache) {
    ApolloClient.Builder()
      .networkTransport(QueueTestNetworkTransport())
      .cache(MemoryCacheFactory())
      .build()
  }

  @After
  fun tearDown() {
    apolloClient.close()
  }

  @Test
  fun `every invocation reaches the network even when an earlier answer is cached`() = runTest {
    val useCase = GetCrossSellsUseCaseImpl(apolloClient)
    apolloClient.enqueueTestResponse(CrossSellsQuery(), data("accident"))
    apolloClient.enqueueTestResponse(CrossSellsQuery(), data("car"))

    assertThat(useCase.invoke()).isRight().transform { it.map(CrossSell::id) }.containsExactly("accident")
    assertThat(useCase.invoke()).isRight().transform { it.map(CrossSell::id) }.containsExactly("car")
  }

  @Test
  fun `an unreachable network falls back to the cached answer`() = runTest {
    val useCase = GetCrossSellsUseCaseImpl(apolloClient)
    apolloClient.enqueueTestResponse(CrossSellsQuery(), data("accident"))
    apolloClient.enqueueTestNetworkError()

    useCase.invoke()

    assertThat(useCase.invoke()).isRight().transform { it.map(CrossSell::id) }.containsExactly("accident")
  }

  private fun data(vararg crossSellIds: String) = CrossSellsQuery.Data(OctopusFakeResolver) {
    currentMember = buildMember {
      crossSellV2 = buildCrossSellV2 {
        otherCrossSells = crossSellIds.map { crossSellId -> buildCrossSell { id = crossSellId } }
      }
    }
  }
}
