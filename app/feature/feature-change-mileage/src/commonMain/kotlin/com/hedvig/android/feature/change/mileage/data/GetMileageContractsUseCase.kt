package com.hedvig.android.feature.change.mileage.data

import arrow.core.Either
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.contract.toContractGroup
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first
import octopus.feature.changemileage.ChangeMileageContractsQuery

internal interface GetMileageContractsUseCase {
  /** The member's active contracts whose yearly mileage can be changed. */
  suspend fun invoke(): Either<ErrorMessage, List<MileageContract>>
}

@ContributesBinding(AppScope::class)
@Inject
internal class GetMileageContractsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetMileageContractsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<MileageContract>> {
    // A deep link reaches the flow without passing the insurance details option, so the flag is checked here too.
    if (!featureManager.isFeatureEnabled(Feature.ENABLE_CHANGE_MILEAGE).first()) {
      return emptyList<MileageContract>().right()
    }
    return apolloClient
      .query(ChangeMileageContractsQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .onLeft { logcat(LogPriority.WARN, it) { "Failed to load contracts for changing mileage" } }
      .mapLeft { ErrorMessage() }
      .map { data ->
        data.currentMember.activeContracts
          .filter { it.supportsChangeMileage }
          .map { contract ->
            MileageContract(
              id = contract.id,
              displayName = contract.currentAgreement.productVariant.displayName,
              exposureDisplayName = contract.exposureDisplayName,
              contractGroup = contract.currentAgreement.productVariant.typeOfContract.toContractGroup(),
            )
          }
      }
  }
}
