package com.hedvig.android.feature.claim.details.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.cross.sell.after.claim.closed.ClosedClaimInfo
import com.hedvig.android.data.cross.sell.after.claim.closed.CrossSellAfterClaimClosedRepository
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import octopus.ClaimQuery
import octopus.builder.Data
import octopus.builder.buildClaim
import octopus.type.ClaimStatus
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetClaimDetailUiStateUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @Test
  fun `a closed claim with the closed flow acknowledges and then triggers the cross-sell on load`() = runTest {
    val events = mutableListOf<String>()
    registerClaim(showClaimClosedFlow = true)

    useCase(events).invoke("claim-1").first()

    assertThat(events).containsExactly("acknowledge:claim-1", "crossSell:claim-1:CLOSED:THEFT:contract-1")
  }

  @Test
  fun `a failed acknowledge does not trigger the cross-sell`() = runTest {
    val events = mutableListOf<String>()
    registerClaim(showClaimClosedFlow = true)

    useCase(events, acknowledgeSucceeds = false).invoke("claim-1").first()

    assertThat(events).containsExactly("acknowledge:claim-1")
  }

  @Test
  fun `a claim without the closed flow neither acknowledges nor triggers the cross-sell`() = runTest {
    val events = mutableListOf<String>()
    registerClaim(showClaimClosedFlow = false)

    useCase(events).invoke("claim-1").first()

    assertThat(events).isEmpty()
  }

  @Test
  fun `hasSubmittedRating is exposed in the content`() = runTest {
    registerClaim(showClaimClosedFlow = false, hasSubmittedRating = true)

    val content = useCase(mutableListOf()).invoke("claim-1").first()

    assertThat(content.getOrNull()?.hasSubmittedRating).isEqualTo(true)
  }

  private fun registerClaim(showClaimClosedFlow: Boolean, hasSubmittedRating: Boolean = false) {
    testApolloClientRule.apolloClient.registerTestResponse(
      ClaimQuery("claim-1"),
      ClaimQuery.Data(OctopusFakeResolver) {
        claim = buildClaim {
          id = "claim-1"
          status = ClaimStatus.CLOSED
          claimType = "THEFT"
          contractId = "contract-1"
          this.showClaimClosedFlow = showClaimClosedFlow
          this.hasSubmittedRating = hasSubmittedRating
          productVariant = null
        }
      },
    )
  }

  private fun useCase(events: MutableList<String>, acknowledgeSucceeds: Boolean = true) =
    GetClaimDetailUiStateUseCaseImpl(
      apolloClient = testApolloClientRule.apolloClient,
      crossSellAfterClaimClosedRepository = object : CrossSellAfterClaimClosedRepository {
        override suspend fun acknowledgeClaimClosedStatus(claimId: String): Either<ErrorMessage, Unit> {
          events.add("acknowledge:$claimId")
          return if (acknowledgeSucceeds) Unit.right() else ErrorMessage().left()
        }

        override fun triggerCrossSell(claim: ClosedClaimInfo) {
          events.add("crossSell:${claim.id}:${claim.status}:${claim.type}:${claim.contractId}")
        }
      },
      clock = Clock.System,
    )
}
