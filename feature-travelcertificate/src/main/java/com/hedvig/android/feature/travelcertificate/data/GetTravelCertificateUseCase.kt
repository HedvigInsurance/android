package com.hedvig.android.feature.travelcertificate.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.common.ErrorMessage

class GetTravelCertificateUseCase(
  private val apolloClient: ApolloClient,
) {

  suspend fun invoke(): Either<ErrorMessage, TravelCertificate> = either {
    TravelCertificate(
      id = "123",
    )
  }
}

data class TravelCertificate(
  val id: String,
)
