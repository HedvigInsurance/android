package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.contract.ContractGroup
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first

internal interface GetGreenCardEligibleContractIdsUseCase {
  suspend fun invoke(): Either<ErrorMessage, Set<String>>
}

// TODO: Replace with GetGreenCardEligibleContractIdsUseCaseImpl once the green card schema is published on
//  Octopus. Until then every car contract is treated as eligible.
@ContributesBinding(AppScope::class)
@Inject
internal class FakeGetGreenCardEligibleContractIdsUseCase(
  private val getInsuranceContractsUseCase: GetInsuranceContractsUseCase,
) : GetGreenCardEligibleContractIdsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, Set<String>> {
    return getInsuranceContractsUseCase.invoke().first().map { contracts ->
      contracts.filter { it.productVariant.contractGroup == ContractGroup.CAR }.map { it.id }.toSet()
    }
  }
}

// @ContributesBinding(AppScope::class)
// @SingleIn(AppScope::class)
// @Inject
// internal class GetGreenCardEligibleContractIdsUseCaseImpl(
//   private val apolloClient: ApolloClient,
// ) : GetGreenCardEligibleContractIdsUseCase {
//   override suspend fun invoke(): Either<ErrorMessage, Set<String>> {
//     return apolloClient.query(GreenCardEligibleContractsQuery())
//       .fetchPolicy(FetchPolicy.NetworkOnly)
//       .safeExecute()
//       .onLeft { logcat(operationError = it) { "GetGreenCardEligibleContractIdsUseCase: $it" } }
//       .mapLeft(::ErrorMessage)
//       .map { data -> data.currentMember.greenCardSpecifications.map { it.contractId }.toSet() }
//   }
// }
