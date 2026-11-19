package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first
import octopus.GreenCardEligibleContractsQuery

internal interface GetGreenCardEligibleContractIdsUseCase {
  suspend fun invoke(): Either<ErrorMessage, Set<String>>
}

@Inject
@SingleIn(AppScope::class)
internal class GetGreenCardEligibleContractIdsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetGreenCardEligibleContractIdsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, Set<String>> {
    if (!featureManager.isFeatureEnabled(Feature.ENABLE_GREEN_CARD).first()) return emptySet<String>().right()
    return apolloClient.query(GreenCardEligibleContractsQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .onLeft { logcat(operationError = it) { "GetGreenCardEligibleContractIdsUseCase: $it" } }
      .mapLeft(::ErrorMessage)
      .map { data -> data.currentMember.greenCardSpecifications.map { it.contractId }.toSet() }
  }
}

@Inject
@SingleIn(AppScope::class)
internal class GetGreenCardEligibleContractIdsUseCaseDemo : GetGreenCardEligibleContractIdsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, Set<String>> = emptySet<String>().right()
}
