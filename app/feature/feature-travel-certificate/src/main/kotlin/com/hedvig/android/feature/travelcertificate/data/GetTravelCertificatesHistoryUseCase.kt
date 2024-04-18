package com.hedvig.android.feature.travelcertificate.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.TravelCertificatesQuery

interface GetTravelCertificatesHistoryUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<TravelCertificate>>
}

internal class GetTravelCertificatesHistoryUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val clock: Clock,
) :
  GetTravelCertificatesHistoryUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<TravelCertificate>> {
    return apolloClient.query(TravelCertificatesQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .toEither(::ErrorMessage)
      .map {
        it.currentMember.travelCertificates.map { certificate ->
          val now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
          TravelCertificate(
            certificate.startDate,
            certificate.id,
            certificate.signedUrl,
            certificate.expiryDate,
            certificate.expiryDate <= now,
          )
        }
      }
  }
}

data class TravelCertificate(
  val startDate: LocalDate,
  val id: String,
  val signedUrl: String,
  val expiryDate: LocalDate,
  val isExpiredNow: Boolean,
)
