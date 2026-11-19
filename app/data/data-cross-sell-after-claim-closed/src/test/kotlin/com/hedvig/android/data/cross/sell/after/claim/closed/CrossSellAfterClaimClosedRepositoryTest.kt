package com.hedvig.android.data.cross.sell.after.claim.closed

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType.ClosedClaim.ClaimInfo
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import octopus.ClaimAcknowledgeClosedStatusMutation
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class CrossSellAfterClaimClosedRepositoryTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  private val crossSellAfterFlowRepository = RecordingCrossSellAfterFlowRepository()

  private fun repository() = CrossSellAfterClaimClosedRepositoryImpl(
    testApolloClientRule.apolloClient,
    crossSellAfterFlowRepository,
  )

  @Test
  fun `triggerCrossSell reports a ClosedClaim with the claim's info and contract`() {
    repository().triggerCrossSell(
      ClosedClaimInfo(
        id = "claimId",
        status = "CLOSED",
        type = "Theft",
        typeOfContract = "SE_APARTMENT_RENT",
        contractId = "contractId",
      ),
    )

    assertThat(crossSellAfterFlowRepository.completedTypes).containsExactly(
      CrossSellInfoType.ClosedClaim(
        ClaimInfo(id = "claimId", status = "CLOSED", type = "Theft", typeOfContract = "SE_APARTMENT_RENT"),
        contractId = "contractId",
      ),
    )
  }

  @Test
  fun `acknowledgeClaimClosedStatus does not trigger the cross-sell`() = runTest {
    testApolloClientRule.apolloClient.registerTestResponse(
      ClaimAcknowledgeClosedStatusMutation("claimId"),
      ClaimAcknowledgeClosedStatusMutation.Data(
        ClaimAcknowledgeClosedStatusMutation.Data.ClaimAcknowledgeClosedStatus(
          __typename = "ClaimMutationOutput",
          userError = null,
        ),
      ),
    )

    val result = repository().acknowledgeClaimClosedStatus("claimId")

    assertThat(result).isRight()
    assertThat(crossSellAfterFlowRepository.completedTypes).isEmpty()
  }

  @Test
  fun `acknowledgeClaimClosedStatus returns Left when the backend responds with a user error`() = runTest {
    testApolloClientRule.apolloClient.registerTestResponse(
      ClaimAcknowledgeClosedStatusMutation("claimId"),
      ClaimAcknowledgeClosedStatusMutation.Data(
        ClaimAcknowledgeClosedStatusMutation.Data.ClaimAcknowledgeClosedStatus(
          __typename = "ClaimMutationOutput",
          userError = ClaimAcknowledgeClosedStatusMutation.Data.ClaimAcknowledgeClosedStatus.UserError(
            __typename = "UserError",
            message = "Not allowed",
          ),
        ),
      ),
    )

    val result = repository().acknowledgeClaimClosedStatus("claimId")

    assertThat(result).isLeft()
    assertThat(crossSellAfterFlowRepository.completedTypes).isEmpty()
  }
}

private class RecordingCrossSellAfterFlowRepository : CrossSellAfterFlowRepository {
  val completedTypes = mutableListOf<CrossSellInfoType>()

  override fun shouldShowCrossSellSheetWithInfo(): Flow<CrossSellInfoType?> = emptyFlow()

  override fun completedCrossSellTriggeringSelfServiceSuccessfully(type: CrossSellInfoType) {
    completedTypes.add(type)
  }

  override fun showedCrossSellSheet(type: CrossSellInfoType?) {}
}
