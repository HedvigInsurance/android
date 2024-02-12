package com.hedvig.android.data.travelcertificate

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import octopus.CoEnsuredForTravelCertificateQuery

interface GetCoEnsuredForTravelCertificateUseCase {
  suspend fun invoke(contractId: String): Either<ErrorMessage, List<TravelCertificateCoEnsured>?>
}

internal class GetCoEnsuredForTravelCertificateUseCaseImpl(private val apolloClient: ApolloClient) :
  GetCoEnsuredForTravelCertificateUseCase {
  override suspend fun invoke(contractId: String): Either<ErrorMessage, List<TravelCertificateCoEnsured>?> {
    return apolloClient.query(CoEnsuredForTravelCertificateQuery(contractId))
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .toEither(::ErrorMessage)
      .map { coEnsuredData ->
        coEnsuredData.contract.coInsured?.map { coEnsured ->
          TravelCertificateCoEnsured("${coEnsured.firstName} ${coEnsured.lastName}", coEnsured.ssn)
        }
      }
  }
}

data class TravelCertificateCoEnsured(
  val fullName: String,
  val ssn: String?,
)
