package com.hedvig.android.feature.travelcertificate.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.travelcertificate.CoInsured
import kotlinx.datetime.LocalDate
import octopus.TravelCertificateCreateMutation
import octopus.type.TravelCertificateCreateCoInsured
import octopus.type.TravelCertificateCreateInput
import slimber.log.e

class CreateTravelCertificateUseCase(
  private val apolloClient: ApolloClient,
) {

  suspend fun invoke(
    contractId: String,
    startDate: LocalDate,
    isMemberIncluded: Boolean,
    coInsured: List<CoInsured>,
    email: String,
  ): Either<ErrorMessage, TravelCertificateUrl> = either {
    val input = TravelCertificateCreateInput(
      contractId = contractId,
      startDate = startDate,
      isMemberIncluded = isMemberIncluded,
      coInsured = coInsured.map { TravelCertificateCreateCoInsured(it.name, it.ssn) },
      email = email,
    )

    val query = TravelCertificateCreateMutation(input)

    val url = apolloClient
      .mutation(query)
      .safeExecute()
      .toEither(::ErrorMessage)
      .onLeft { e { it.message ?: "Could not create travel certificate" } }
      .bind()
      .travelCertificateCreate
      .signedUrl

    TravelCertificateUrl(url)
  }
}

@JvmInline
value class TravelCertificateUrl(val url: String)
