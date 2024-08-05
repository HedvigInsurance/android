package com.hedvig.android.feature.travelcertificate.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.EligibleContractsWithAddressQuery

interface GetEligibleContractsWithAddressUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<ContractEligibleWithAddress>>
}

internal class GetEligibleContractsWithAddressUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetEligibleContractsWithAddressUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<ContractEligibleWithAddress>> {
    return apolloClient.query(EligibleContractsWithAddressQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .toEither(::ErrorMessage)
      .map { data ->
        data.currentMember.activeContracts.filter { contract ->
          contract.supportsTravelCertificate
        }.map { contract ->
          val street =
            data.currentMember.travelCertificateSpecifications.contractSpecifications.firstOrNull {
              it.contractId == contract.id
            }?.location?.street
          val address = street ?: contract.exposureDisplayName.substringBefore("•").also {
            logcat(LogPriority.ERROR) {
              "Received a travel certificate eligible contract, " +
                "but the contractSpecifications did not include a valid street name"
            }
          }
          ContractEligibleWithAddress(address, contract.id)
        }
      }
  }
}

data class ContractEligibleWithAddress(
  val address: String,
  val contractId: String,
)
