package com.hedvig.android.data.travelcertificate

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.supportsTravelCertificate
import com.hedvig.android.data.contract.toContractType
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.CurrentContractsQuery

interface CheckTravelCertificateAvailabilityUseCase {
  suspend fun invoke(): Either<TravelCertificateAvailabilityError, Boolean>
}

internal class CheckTravelCertificateAvailabilityUseCaseImpl(
  private val getTravelCertificatesHistory: GetTravelCertificatesHistoryUseCase,
  val apolloClient: ApolloClient,
) : CheckTravelCertificateAvailabilityUseCase {
  override suspend fun invoke(): Either<TravelCertificateAvailabilityError, Boolean> {
    return either {
      val contracts = apolloClient.query(CurrentContractsQuery())
        .safeExecute()
        .toEither(::ErrorMessage)
        .onLeft {
          logcat(LogPriority.ERROR) { it.message ?: "Could not fetch current contracts" }
        }.getOrNull()?.currentMember?.activeContracts

      val contractsWithTravelCertificate = contracts
        ?.map { it.currentAgreement.productVariant.typeOfContract.toContractType() }
        ?.firstOrNull { it.supportsTravelCertificate() }

      val history = getTravelCertificatesHistory.invoke().value

      ensure(history.isNotEmpty() || contractsWithTravelCertificate != null) {
        TravelCertificateAvailabilityError.TravelCertificateNotAvailable
      }

      true
    }
  }
}

sealed interface TravelCertificateAvailabilityError {
  data object TravelCertificateNotAvailable : TravelCertificateAvailabilityError

  data class Error(
    val errorMessage: ErrorMessage,
  ) : TravelCertificateAvailabilityError, ErrorMessage by errorMessage
}
