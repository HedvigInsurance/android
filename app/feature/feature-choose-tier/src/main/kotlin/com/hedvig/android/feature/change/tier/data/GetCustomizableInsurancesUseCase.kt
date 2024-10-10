package com.hedvig.android.feature.change.tier.data

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.toContractGroup
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import octopus.ContractsEligibleForTierChangeQuery

interface GetCustomizableInsurancesUseCase {
  suspend fun invoke(): Flow<Either<ErrorMessage, NonEmptyList<CustomisableInsurance>?>>
}

internal class GetCustomizableInsurancesUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetCustomizableInsurancesUseCase {
  override suspend fun invoke(): Flow<Either<ErrorMessage, NonEmptyList<CustomisableInsurance>?>> {
    return combine(
      featureManager.isFeatureEnabled(Feature.TIER),
      apolloClient
        .query(ContractsEligibleForTierChangeQuery())
        .safeFlow(::ErrorMessage),
    ) { isEnabled, memberResponse ->
      either {
        if (!isEnabled) return@either null
        memberResponse.bind().currentMember.toInsurancesForChangingTier().toNonEmptyListOrNull()
      }
    }
  }
}

data class CustomisableInsurance(
  val id: String,
  val displayName: String,
  val contractExposure: String,
  val contractGroup: ContractGroup,
)

private fun ContractsEligibleForTierChangeQuery.Data.CurrentMember.toInsurancesForChangingTier():
  List<CustomisableInsurance> {
  return activeContracts
    .filter {
      it.supportsChangeTier
    }
    .map {
      CustomisableInsurance(
        id = it.id,
        displayName = it.currentAgreement.productVariant.displayName,
        contractGroup = it.currentAgreement.productVariant.typeOfContract.toContractGroup(),
        contractExposure = it.exposureDisplayName,
      )
    }
}
