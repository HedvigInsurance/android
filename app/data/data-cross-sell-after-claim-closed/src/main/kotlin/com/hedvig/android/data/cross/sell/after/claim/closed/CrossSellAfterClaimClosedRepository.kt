package com.hedvig.android.data.cross.sell.after.claim.closed

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType.ClosedClaim.ClaimInfo
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.ClaimAcknowledgeClosedStatusMutation
import octopus.fragment.ClaimFragment

interface CrossSellAfterClaimClosedRepository {
  suspend fun acknowledgeClaimClosedStatus(claim: ClaimFragment): Either<ErrorMessage, Unit>
}

class CrossSellAfterClaimClosedRepositoryImpl(
  private val apolloClient: ApolloClient,
  private val crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
) : CrossSellAfterClaimClosedRepository {
  override suspend fun acknowledgeClaimClosedStatus(claim: ClaimFragment): Either<ErrorMessage, Unit> {
    return apolloClient
      .mutation(ClaimAcknowledgeClosedStatusMutation(claim.id))
      .safeExecute(::ErrorMessage)
      .map { response ->
        either {
          val userError = response
            .claimAcknowledgeClosedStatus
            ?.userError
          if (userError != null) {
            logcat(LogPriority.ERROR) { "ClaimAcknowledgeClosedStatusMutation failed: ${userError.message}" }
            raise(ErrorMessage(userError.message))
          }
          crossSellAfterFlowRepository.completedCrossSellTriggeringSelfServiceSuccessfully(
            CrossSellInfoType.ClosedClaim(
              ClaimInfo(
                claim.id,
                claim.status?.name,
                claim.claimType,
                claim.productVariant?.typeOfContract,
              ),
            ),
          )
          Unit
        }
      }
  }
}
