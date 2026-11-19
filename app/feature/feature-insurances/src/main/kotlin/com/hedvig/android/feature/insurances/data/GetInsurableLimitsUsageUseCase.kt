package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.InsurableLimitsUsageQuery
import octopus.fragment.InsurableLimitUsageFragment

internal interface GetInsurableLimitsUsageUseCase {
  /**
   * Right(null) when the contract has no usage to show, which the backend returns for everything but pet contracts.
   */
  suspend fun invoke(contractId: String): Either<ErrorMessage, InsurableLimitsUsage?>
}

@Inject
@SingleIn(AppScope::class)
internal class GetInsurableLimitsUsageUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetInsurableLimitsUsageUseCase {
  override suspend fun invoke(contractId: String): Either<ErrorMessage, InsurableLimitsUsage?> {
    return either {
      val usage = apolloClient
        .query(InsurableLimitsUsageQuery(contractId))
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute(::ErrorMessage)
        .onLeft { logcat(LogPriority.ERROR) { "Failed to load insurable limits usage: $it" } }
        .bind()
        .contract
        .insurableLimitsUsage
        ?: return@either null
      InsurableLimitsUsage(
        periodStartDate = usage.periodStartDate,
        periodEndDate = usage.periodEndDate,
        total = usage.total.toLimitUsage(),
        medicine = usage.medicine?.toLimitUsage(),
        deductible = usage.deductible?.toLimitUsage(),
      )
    }
  }
}

private fun InsurableLimitUsageFragment.toLimitUsage(): InsurableLimitsUsage.LimitUsage {
  return InsurableLimitsUsage.LimitUsage(
    used = UiMoney.fromMoneyFragment(used),
    limit = UiMoney.fromMoneyFragment(limit),
    remaining = UiMoney.fromMoneyFragment(remaining),
  )
}
