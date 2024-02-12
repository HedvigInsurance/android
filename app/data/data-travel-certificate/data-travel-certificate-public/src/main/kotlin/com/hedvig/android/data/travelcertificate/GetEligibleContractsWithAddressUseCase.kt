package com.hedvig.android.data.travelcertificate

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
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
      .map {
        it.currentMember.activeContracts.filter { contract ->
          contract.supportsTravelCertificate
        }.map { contract ->
          ContractEligibleWithAddress(contract.exposureDisplayName.substringBefore("•"), contract.id)
        }
      }
  }
}

data class ContractEligibleWithAddress(
  val address: String,
  val contractId: String,
)
