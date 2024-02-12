package com.hedvig.android.feature.travelcertificate.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.travelcertificate.ui.generate.CoInsured
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.TravelCertificateCreateMutation
import octopus.type.TravelCertificateCreateCoInsured
import octopus.type.TravelCertificateCreateInput

internal class CreateTravelCertificateUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(
    contractId: String,
    startDate: LocalDate,
    isMemberIncluded: Boolean,
    coInsured: List<CoInsured>,
    email: String,
  ): Either<ErrorMessage, TravelCertificateUrl> = withContext(Dispatchers.IO) {
    either {
      val input = TravelCertificateCreateInput(
        contractId = contractId,
        startDate = startDate,
        isMemberIncluded = isMemberIncluded,
        coInsured = coInsured.map { TravelCertificateCreateCoInsured(it.name, it.ssn) },
        email = email,
      )

      val query = TravelCertificateCreateMutation(input)

      val pdfUrl = apolloClient
        .mutation(query)
        .safeExecute()
        .toEither(::ErrorMessage)
        .onLeft {
          logcat(throwable = it.throwable) {
            "CreateTravelCertificateUseCase: ${it.message ?: "Could not create travel certificate"}"
          }
        }
        .bind()
        .travelCertificateCreate
        .signedUrl

      TravelCertificateUrl(pdfUrl)
    }
  }
}

@JvmInline
@Serializable
internal value class TravelCertificateUrl(val uri: String)
