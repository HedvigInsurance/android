package com.hedvig.android.feature.greencard.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.GreenCardSpecificationsQuery

internal interface GetGreenCardSpecificationsUseCase {
  suspend fun invoke(): Either<ErrorMessage, GreenCardSpecifications>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetGreenCardSpecificationsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetGreenCardSpecificationsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, GreenCardSpecifications> {
    return apolloClient.query(GreenCardSpecificationsQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .onLeft { logcat(operationError = it) { "GetGreenCardSpecificationsUseCase: $it" } }
      .mapLeft(::ErrorMessage)
      .map { data ->
        GreenCardSpecifications(
          contracts = data.currentMember.greenCardSpecifications.map {
            GreenCardContractSpecification(
              contractId = it.contractId,
              registrationNumber = it.registrationNumber,
              minStartDate = it.minStartDate,
              maxEndDate = it.maxEndDate,
              maxPeriodDays = it.maxPeriod,
            )
          },
        )
      }
  }
}
