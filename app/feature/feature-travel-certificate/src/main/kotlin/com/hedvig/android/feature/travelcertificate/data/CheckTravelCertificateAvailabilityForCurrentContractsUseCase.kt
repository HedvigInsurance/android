package com.hedvig.android.feature.travelcertificate.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.CurrentContractsQuery

interface CheckTravelCertificateAvailabilityForCurrentContractsUseCase {
  suspend fun invoke(): Either<ErrorMessage, Boolean>
}

internal class CheckTravelCertificateAvailabilityForCurrentContractsUseCaseImpl(
  val apolloClient: ApolloClient,
) : CheckTravelCertificateAvailabilityForCurrentContractsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, Boolean> {
    return either {
      val contracts = apolloClient.query(CurrentContractsQuery())
        .safeExecute {
          ErrorMessage("CheckTravelCertificateAvailabilityForCurrentContractsUseCase: $it")
        }
        .map {
          it.currentMember.activeContracts
        }
        .onLeft { errorMessage ->
          logcat(priority = LogPriority.ERROR) { "Could not fetch current contracts. Message:${errorMessage.message}" }
        }
        .bind()
      val hasContractWhichSupportsTravelCertificates: Boolean = contracts
        .any { it.supportsTravelCertificate }

      hasContractWhichSupportsTravelCertificates
    }
  }
}
