package com.hedvig.android.data.termination.data

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
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
import octopus.ContractsToTerminateQuery

interface GetTerminatableContractsUseCase {
  suspend fun invoke(): Flow<Either<ErrorMessage, NonEmptyList<TerminatableInsurance>?>>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetTerminatableContractsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetTerminatableContractsUseCase {
  override suspend fun invoke(): Flow<Either<ErrorMessage, NonEmptyList<TerminatableInsurance>?>> {
    return apolloClient
      .query(ContractsToTerminateQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeFlow(::ErrorMessage)
      .map { memberResponse ->
        either {
          memberResponse.bind().currentMember.toInsurancesForCancellation().toNonEmptyListOrNull()
        }
      }
  }
}

data class TerminatableInsurance(
  val id: String,
  val displayName: String,
  val contractExposure: String,
  val contractGroup: ContractGroup,
)

private fun ContractsToTerminateQuery.Data.CurrentMember.toInsurancesForCancellation(): List<TerminatableInsurance> {
  val active = activeContracts
    .filter {
      it.supportsTermination
    }
    .map {
      TerminatableInsurance(
        id = it.id,
        displayName = it.currentAgreement.productVariant.displayName,
        contractGroup = it.currentAgreement.productVariant.typeOfContract.toContractGroup(),
        contractExposure = it.exposureDisplayName,
      )
    }
  val pending = pendingContracts.map {
    TerminatableInsurance(
      id = it.id,
      displayName = it.productVariant.displayName,
      contractGroup = it.productVariant.typeOfContract.toContractGroup(),
      contractExposure = it.exposureDisplayName,
    )
  }
  return active + pending
}
