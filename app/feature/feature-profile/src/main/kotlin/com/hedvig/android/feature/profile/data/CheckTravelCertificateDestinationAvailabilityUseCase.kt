package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.TravelCertificateAvailabilityQuery

interface CheckTravelCertificateDestinationAvailabilityUseCase {
  suspend fun invoke(): Either<TravelCertificateAvailabilityError, Unit>
}

internal class CheckTravelCertificateDestinationAvailabilityUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CheckTravelCertificateDestinationAvailabilityUseCase {
  override suspend fun invoke(): Either<TravelCertificateAvailabilityError, Unit> {
    return either {

      val isTravelCertificateEnabledResult = apolloClient.query(TravelCertificateAvailabilityQuery())
        .safeExecute()
        .toEither { message, _ ->
          ErrorMessage("Could not check isTravelCertificateEnabled: $message")
        }
        .map {
          it.currentMember.memberActions?.isTravelCertificateEnabled
        }
        .onLeft { errorMessage ->
          logcat(priority = LogPriority.ERROR) { "CheckTravelCertificateAvailability error. Message: ${errorMessage.message}" }
          raise(TravelCertificateAvailabilityError.Error(errorMessage))
        }
      val isTravelCertificateEnabled = isTravelCertificateEnabledResult.getOrNull() == true
      ensure(isTravelCertificateEnabled) {
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
