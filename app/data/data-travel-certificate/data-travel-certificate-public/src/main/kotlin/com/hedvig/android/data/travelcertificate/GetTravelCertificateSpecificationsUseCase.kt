package com.hedvig.android.data.travelcertificate

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import kotlinx.datetime.LocalDate
import octopus.TravelCertificateSpecificationsQuery

interface GetTravelCertificateSpecificationsUseCase {
  suspend fun invoke(): Either<TravelCertificateError, TravelCertificateData>
}

internal class GetTravelCertificateSpecificationsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetTravelCertificateSpecificationsUseCase {
  override suspend fun invoke(): Either<TravelCertificateError, TravelCertificateData> {
    return either {
      val member = apolloClient
        .query(TravelCertificateSpecificationsQuery())
        .safeExecute()
        .toEither(::ErrorMessage)
        .mapLeft(TravelCertificateError::Error)
        .onLeft {
          logcat(throwable = it.throwable) {
            "GetTravelCertificateSpecificationsUseCaseImpl: ${it.message ?: "Could not fetch travel certificate"}"
          }
        }
        .bind()
        .currentMember

      val travelCertificateSpecifications = member.travelCertificateSpecifications.contractSpecifications.firstOrNull()
      ensureNotNull(travelCertificateSpecifications) { TravelCertificateError.NotEligible }
      TravelCertificateData(
        travelCertificateSpecification = travelCertificateSpecifications.toTravelCertificateSpecification(
          member.email,
        ),
        infoSections = member.travelCertificateSpecifications.infoSpecifications.map {
          TravelCertificateData.InfoSection(
            it.title,
            it.body,
          )
        },
      )
    }
  }
}

@Suppress("ktlint:standard:max-line-length")
private fun TravelCertificateSpecificationsQuery.Data.CurrentMember.TravelCertificateSpecifications.ContractSpecification.toTravelCertificateSpecification(
  email: String,
) = TravelCertificateData.TravelCertificateSpecification(
  contractId = contractId,
  email = email,
  maxDurationDays = maxDurationDays,
  dateRange = minStartDate..maxStartDate,
  numberOfCoInsured = numberOfCoInsured,
)

sealed interface TravelCertificateError {
  data class Error(
    val errorMessage: ErrorMessage,
  ) : TravelCertificateError, ErrorMessage by errorMessage

  object NotEligible : TravelCertificateError
}

data class TravelCertificateData(
  val travelCertificateSpecification: TravelCertificateSpecification,
  val infoSections: List<InfoSection>,
) {
  data class InfoSection(
    val title: String,
    val body: String,
  )

  data class TravelCertificateSpecification(
    val contractId: String,
    val email: String,
    val maxDurationDays: Int,
    val dateRange: ClosedRange<LocalDate>,
    val numberOfCoInsured: Int,
  )
}
