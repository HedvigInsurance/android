package com.hedvig.android.feature.greencard.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

internal interface GetGreenCardSpecificationsUseCase {
  suspend fun invoke(): Either<ErrorMessage, GreenCardSpecifications>
}

// TODO: Replace with GetGreenCardSpecificationsUseCaseImpl once the green card schema is published on Octopus.
@ContributesBinding(AppScope::class)
@Inject
internal class FakeGetGreenCardSpecificationsUseCase(
  private val fakeGreenCardBackend: FakeGreenCardBackend,
) : GetGreenCardSpecificationsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, GreenCardSpecifications> {
    return GreenCardSpecifications(fakeGreenCardBackend.specifications()).right()
  }
}

// @ContributesBinding(AppScope::class)
// @SingleIn(AppScope::class)
// @Inject
// internal class GetGreenCardSpecificationsUseCaseImpl(
//   private val apolloClient: ApolloClient,
// ) : GetGreenCardSpecificationsUseCase {
//   override suspend fun invoke(): Either<ErrorMessage, GreenCardSpecifications> {
//     return apolloClient.query(GreenCardSpecificationsQuery())
//       .fetchPolicy(FetchPolicy.NetworkOnly)
//       .safeExecute()
//       .onLeft { logcat(operationError = it) { "GetGreenCardSpecificationsUseCase: $it" } }
//       .mapLeft(::ErrorMessage)
//       .map { data ->
//         GreenCardSpecifications(
//           contracts = data.currentMember.greenCardSpecifications.map {
//             GreenCardContractSpecification(
//               contractId = it.contractId,
//               registrationNumber = it.registrationNumber,
//               minStartDate = it.minStartDate,
//               maxEndDate = it.maxEndDate,
//             )
//           },
//         )
//       }
//   }
// }
