package com.hedvig.android.feature.travelcertificate.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.supportsTravelCertificate
import com.hedvig.android.data.contract.toContractType
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
        .safeExecute()
        .toEither { message, _ ->
          ErrorMessage("CheckTravelCertificateAvailabilityForCurrentContractsUseCase: $message")
        }
        .map {
          it.currentMember.activeContracts
        }
        .onLeft { errorMessage ->
          logcat(priority = LogPriority.ERROR) { "Could not fetch current contracts. Message:${errorMessage.message}" }
        }
        .bind()
      val hasContractWhichSupportsTravelCertificates: Boolean = contracts
        .map { it.currentAgreement.productVariant.typeOfContract.toContractType() }
        .any { it.supportsTravelCertificate() }

      hasContractWhichSupportsTravelCertificates
    }
  }
}
