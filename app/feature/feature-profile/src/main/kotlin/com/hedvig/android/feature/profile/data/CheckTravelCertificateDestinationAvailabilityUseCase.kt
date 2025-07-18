package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.TravelCertificateAvailabilityQuery

internal interface CheckTravelCertificateDestinationAvailabilityUseCase {
  suspend fun invoke(): Either<TravelCertificateAvailabilityError, Unit>
}

internal class CheckTravelCertificateDestinationAvailabilityUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CheckTravelCertificateDestinationAvailabilityUseCase {
  override suspend fun invoke(): Either<TravelCertificateAvailabilityError, Unit> {
    return either {
      val isTravelCertificateEnabled = apolloClient.query(TravelCertificateAvailabilityQuery())
        .safeExecute()
        .mapLeft {
          logcat(LogPriority.ERROR, it) { "CheckTravelCertificateAvailability error. Message: $it" }
          val errorMessage = ErrorMessage("Could not check isTravelCertificateEnabled: $it")
          raise(TravelCertificateAvailabilityError.Error(errorMessage))
        }
        .map {
          it.currentMember.memberActions?.isTravelCertificateEnabled
        }
        .bind()
      ensure(isTravelCertificateEnabled == true) {
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
