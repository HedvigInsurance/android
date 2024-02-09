package com.hedvig.android.data.travelcertificate

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

interface CheckTravelCertificateDestinationAvailabilityUseCase {
  suspend fun invoke(): Either<TravelCertificateAvailabilityError, Unit>
}

internal class CheckTravelCertificateDestinationAvailabilityUseCaseImpl(
  private val getTravelCertificatesHistory: GetTravelCertificatesHistoryUseCase,
  private val checkTravelCertificateAvailabilityForCurrentContractsUseCase: CheckTravelCertificateAvailabilityForCurrentContractsUseCase,
) : CheckTravelCertificateDestinationAvailabilityUseCase {
  override suspend fun invoke(): Either<TravelCertificateAvailabilityError, Unit> {
    return either {
      val contractsResponse =
        checkTravelCertificateAvailabilityForCurrentContractsUseCase.invoke().onLeft { errorMessage ->
          logcat { "Could not fetch current contracts to check travel certificate availability. Message:${errorMessage.message}" }
        }
      val historyResult = getTravelCertificatesHistory.invoke()
        .onLeft { errorMessage ->
          logcat(LogPriority.ERROR) { "Could not fetch travel certificates history: ${errorMessage.message}" }
        }

      if (contractsResponse is Either.Left<ErrorMessage> && historyResult is Either.Left<ErrorMessage>) {
        raise(
          TravelCertificateAvailabilityError.Error(
            ErrorMessage(
              "TravelCertificateAvailabilityError: ${contractsResponse.value.message} && ${historyResult.value.message}",
            ),
          ),
        )
      }

      val hasContractWhichSupportsTravelCertificates = contractsResponse.getOrNull() ?: false
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
