package com.hedvig.android.feature.chip.id.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.toContractGroup
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import octopus.GetPetContractsForChipIdQuery

internal interface GetPetContractsForChipIdUseCase {
  suspend fun invoke(): Either<ApolloOperationError, List<PetContractForChipId>>
}

internal class GetPetContractsForChipIdUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetPetContractsForChipIdUseCase {
  override suspend fun invoke(): Either<ApolloOperationError, List<PetContractForChipId>> {
    val flow: Flow<Either<ApolloOperationError, List<PetContractForChipId>>> = apolloClient
      .query(GetPetContractsForChipIdQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeFlow()
      .map { result ->
        result.map { data ->
          data.currentMember.activeContracts
            .mapNotNull { contract ->
              val contractGroup = contract.currentAgreement.productVariant.typeOfContract.toContractGroup()
              if (contractGroup == ContractGroup.CAT || contractGroup == ContractGroup.DOG) {
                PetContractForChipId(
                  id = contract.id,
                  displayName = contract.currentAgreement.productVariant.displayName,
                  contractExposure = contract.exposureDisplayNameShort,
                  contractGroup = contractGroup,
                )
              } else {
                null
              }
            }
        }.onLeft { error ->
          logcat(operationError = error) { "GetPetContractsForChipIdUseCase failed with $error" }
        }
      }

    return flow.first()
  }
}
