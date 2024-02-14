package com.hedvig.android.data.travelcertificate

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.common.ErrorMessage

interface GetEligibleContractsWithAddressUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<ContractEligibleWithAddress>>
}

internal class GetEligibleContractsWithAddressUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetEligibleContractsWithAddressUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<ContractEligibleWithAddress>> {
    // todo: remove mock!
    return either {
      listOf(
        ContractEligibleWithAddress("Morbydalen 12", "keuwhwkjfhjkeharfj"),
        ContractEligibleWithAddress("Akerbyvagen 257", "sesjhfhakerfhlwkeija"),
      )
    }

//    return apolloClient.query(EligibleContractsWithAddressQuery())
//      .fetchPolicy(FetchPolicy.NetworkOnly)
//      .safeExecute()
//      .toEither(::ErrorMessage)
//      .map {
//        it.currentMember.activeContracts.filter { contract ->
//          contract.supportsTravelCertificate
//        }.map { contract ->
//          ContractEligibleWithAddress(contract.exposureDisplayName.substringBefore("•"), contract.id)
//        }
//      }
  }
}

data class ContractEligibleWithAddress(
  val address: String,
  val contractId: String,
)
