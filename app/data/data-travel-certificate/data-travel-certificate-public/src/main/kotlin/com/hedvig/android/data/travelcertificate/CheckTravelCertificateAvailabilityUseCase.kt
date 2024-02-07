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
  suspend fun invoke(): Either<TravelCertificateAvailabilityError, Unit>
}

internal class CheckTravelCertificateAvailabilityUseCaseImpl(
  private val getTravelCertificatesHistory: GetTravelCertificatesHistoryUseCase,
  val apolloClient: ApolloClient,
) : CheckTravelCertificateAvailabilityUseCase {
  override suspend fun invoke(): Either<TravelCertificateAvailabilityError, Unit> {
    return either {
      val contractsResult = apolloClient.query(CurrentContractsQuery())
        .safeExecute()
        .toEither(::ErrorMessage)
        .map {
          it.currentMember.activeContracts
        }
        .onLeft { errorMessage ->
          logcat { "Could not fetch current contracts. Message:${errorMessage.message}" }
        }
      val historyResult = getTravelCertificatesHistory.invoke()
        .onLeft { errorMessage ->
          logcat(LogPriority.ERROR) { "Could not fetch travel certificates history: ${errorMessage.message}" }
        }

      if (contractsResult is Either.Left<ErrorMessage> && historyResult is Either.Left<ErrorMessage>) {
        raise(
          TravelCertificateAvailabilityError.Error(
            ErrorMessage(
              "TravelCertificateAvailabilityError: ${contractsResult.value.message} && ${historyResult.value.message}",
            ),
          ),
        )
      }

      val hasContractWhichSupportsTravelCertificates: Boolean = contractsResult.getOrNull()
        ?.map { it.currentAgreement.productVariant.typeOfContract.toContractType() }
        ?.any { it.supportsTravelCertificate() } ?: false

      val hasCertificateHistory = !historyResult.getOrNull().isNullOrEmpty()

      ensure(hasCertificateHistory || hasContractWhichSupportsTravelCertificates) {
        TravelCertificateAvailabilityError.TravelCertificateNotAvailable
      }
    }
  }
}

sealed interface TravelCertificateAvailabilityError {
  data object TravelCertificateNotAvailable : TravelCertificateAvailabilityError

  data class Error(
    val errorMessage: ErrorMessage,
  ) : TravelCertificateAvailabilityError, ErrorMessage by errorMessage
}
