package com.hedvig.android.feature.change.tier.data

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.toContractGroup
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import octopus.ContractsEligibleForTierChangeQuery

internal interface GetCustomizableInsurancesUseCase {
  suspend fun invoke(): Flow<Either<ErrorMessage, NonEmptyList<CustomisableInsurance>?>>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetCustomizableInsurancesUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCustomizableInsurancesUseCase {
  override suspend fun invoke(): Flow<Either<ErrorMessage, NonEmptyList<CustomisableInsurance>?>> {
    return apolloClient
      .query(ContractsEligibleForTierChangeQuery())
      .safeFlow(::ErrorMessage)
      .map { memberResponse ->
        either {
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
