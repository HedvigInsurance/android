package com.hedvig.android.feature.chip.id.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.logger.logcat
import octopus.GetPetContractsForChipIdQuery

internal interface GetPetContractsForChipIdUseCase {
  suspend fun invoke(): Either<ApolloOperationError, List<PetContractForChipId>>
}

internal class GetPetContractsForChipIdUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetPetContractsForChipIdUseCase {
  override suspend fun invoke(): Either<ApolloOperationError, List<PetContractForChipId>> {
    return apolloClient
      .query(GetPetContractsForChipIdQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .map { data ->
        data.currentMember.activeContracts
          .mapNotNull { contract ->
            if (contract.missingPetId) {
              val result = PetContractForChipId(
                id = contract.id,
                displayName = contract.currentAgreement.productVariant.displayName,
                contractExposure = contract.exposureDisplayNameShort,
              )
              logcat {
                "GetPetContractsForChipIdUseCaseImpl: result: $result contract.missingPetId: ${contract.missingPetId}"
              }
              result
            } else {
              null
            }
          }
      }.onLeft { error ->
        logcat(operationError = error) { "GetPetContractsForChipIdUseCase failed with $error" }
      }
  }
}
