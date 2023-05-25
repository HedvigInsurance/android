package com.hedvig.android.feature.travelcertificate.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.datetime.LocalDate
import octopus.TravelCertificateSpecificationsQuery
import slimber.log.e

class GetTravelCertificateSpecificationsUseCase(
  private val apolloClient: ApolloClient,
) {

  private val query = TravelCertificateSpecificationsQuery()

  suspend fun invoke(): Either<ErrorMessage, TravelCertificateResult> = either {
    val member = apolloClient
      .query(query)
      .safeExecute()
      .toEither(::ErrorMessage)
      .onLeft { e { it.message ?: "Could not fetch travel certificate" } }
      .bind()
      .currentMember

    when (val travelCertificateSpecifications =
      member.travelCertificateSpecifications.contractSpecifications.firstOrNull()) {
      null -> TravelCertificateResult.NotEligible
      else -> TravelCertificateResult.TraverlCertificateData(
        travelCertificateSpecification = travelCertificateSpecifications.toTravelCertificateSpecification(member.email),
        infoSections = member.travelCertificateSpecifications.infoSpecifications.map {
          TravelCertificateResult.TraverlCertificateData.InfoSection(
            it.title,
            it.body,
          )
        },
      )
    }
  }
}

// ktlint-disable max-line-length
private fun TravelCertificateSpecificationsQuery.Data.CurrentMember.TravelCertificateSpecifications.ContractSpecification.toTravelCertificateSpecification(
  email: String,
) = TravelCertificateResult.TraverlCertificateData.TravelCertificateSpecification(
  contractId = contractId,
  email = email,
  maxDurationDays = maxDurationDays,
  dateRange = minStartDate..maxStartDate,
  numberOfCoInsured = numberOfCoInsured,
)

sealed interface TravelCertificateResult {

  data class TraverlCertificateData(
    val travelCertificateSpecification: TravelCertificateSpecification,
    val infoSections: List<InfoSection>,
  ) : TravelCertificateResult {
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


  object NotEligible : TravelCertificateResult
}
