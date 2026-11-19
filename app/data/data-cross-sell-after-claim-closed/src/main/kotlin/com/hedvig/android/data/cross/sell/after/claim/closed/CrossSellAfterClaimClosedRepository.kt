package com.hedvig.android.data.cross.sell.after.claim.closed

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType.ClosedClaim.ClaimInfo
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.ClaimAcknowledgeClosedStatusMutation

data class ClosedClaimInfo(
  val id: String,
  val status: String?,
  val type: String?,
  val typeOfContract: String?,
  val contractId: String?,
)

interface CrossSellAfterClaimClosedRepository {
  suspend fun acknowledgeClaimClosedStatus(claimId: String): Either<ErrorMessage, Unit>

  fun triggerCrossSell(claim: ClosedClaimInfo)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class CrossSellAfterClaimClosedRepositoryImpl(
  private val apolloClient: ApolloClient,
  private val crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
) : CrossSellAfterClaimClosedRepository {
  override suspend fun acknowledgeClaimClosedStatus(claimId: String): Either<ErrorMessage, Unit> = either {
    val response = apolloClient
      .mutation(ClaimAcknowledgeClosedStatusMutation(claimId))
      .safeExecute(::ErrorMessage)
      .bind()
    val userError = response.claimAcknowledgeClosedStatus?.userError
    if (userError != null) {
      logcat(LogPriority.ERROR) { "ClaimAcknowledgeClosedStatusMutation failed: ${userError.message}" }
      raise(ErrorMessage(userError.message))
    }
  }

  override fun triggerCrossSell(claim: ClosedClaimInfo) {
    crossSellAfterFlowRepository.completedCrossSellTriggeringSelfServiceSuccessfully(
      CrossSellInfoType.ClosedClaim(
        ClaimInfo(
          id = claim.id,
          status = claim.status,
          type = claim.type,
          typeOfContract = claim.typeOfContract,
        ),
        contractId = claim.contractId,
      ),
    )
  }
}
