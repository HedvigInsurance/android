package com.hedvig.android.feature.travelcertificate.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
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
  ): Either<ErrorMessage, TravelCertificateUrl> = either {
    val input = TravelCertificateCreateInput(
      contractId = contractId,
      startDate = startDate,
      isMemberIncluded = isMemberIncluded,
      coInsured = coInsured.map {
        TravelCertificateCreateCoInsured(
          fullName = it.name,
          ssn = Optional.present(it.ssn),
          dateOfBirth = Optional.present(it.dateOfBirth),
        )
      },
      email = email,
    )

    val pdfUrl = apolloClient
      .mutation(TravelCertificateCreateMutation(input))
      .safeExecute()
      .onLeft {
        logcat(LogPriority.ERROR, it) {
          "CreateTravelCertificateUseCase: $it"
        }
      }
      .mapLeft(::ErrorMessage)
      .bind()
      .travelCertificateCreate
      .signedUrl

    TravelCertificateUrl(pdfUrl)
  }
}

@JvmInline
@Serializable
internal value class TravelCertificateUrl(val uri: String)
