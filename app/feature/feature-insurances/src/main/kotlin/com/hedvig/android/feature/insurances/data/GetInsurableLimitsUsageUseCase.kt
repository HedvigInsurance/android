package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

internal interface GetInsurableLimitsUsageUseCase {
  /**
   * Right(null) when the contract has no usage to show, which the backend returns for everything but pet contracts.
   */
  suspend fun invoke(contractId: String): Either<ErrorMessage, InsurableLimitsUsage?>
}

// TODO: Switch to the real query once `Contract.insurableLimitsUsage` is published in the Octopus schema:
//  uncomment QueryInsurableLimitsUsage.graphql, this class's body and its imports, and delete the fake return.
@Inject
@SingleIn(AppScope::class)
internal class GetInsurableLimitsUsageUseCaseImpl(
  // private val apolloClient: ApolloClient,
) : GetInsurableLimitsUsageUseCase {
  override suspend fun invoke(contractId: String): Either<ErrorMessage, InsurableLimitsUsage?> {
    return fakeInsurableLimitsUsage.right()
    // return either {
    //   val usage = apolloClient
    //     .query(InsurableLimitsUsageQuery(contractId))
    //     .fetchPolicy(FetchPolicy.NetworkOnly)
    //     .safeExecute(::ErrorMessage)
    //     .onLeft { logcat(LogPriority.ERROR) { "Failed to load insurable limits usage: $it" } }
    //     .bind()
    //     .contract
    //     .insurableLimitsUsage
    //     ?: return@either null
    //   InsurableLimitsUsage(
    //     periodStartDate = usage.periodStartDate,
    //     periodEndDate = usage.periodEndDate,
    //     total = usage.total.toLimitUsage(),
    //     medicine = usage.medicine?.toLimitUsage(),
    //     deductible = usage.deductible.toLimitUsage(),
    //   )
    // }
  }
}

// private fun InsurableLimitUsageFragment.toLimitUsage(): InsurableLimitsUsage.LimitUsage {
//   return InsurableLimitsUsage.LimitUsage(
//     used = UiMoney.fromMoneyFragment(used),
//     limit = UiMoney.fromMoneyFragment(limit),
//     remaining = UiMoney.fromMoneyFragment(remaining),
//   )
// }

// Imports needed once the code above is uncommented:
// import arrow.core.raise.either
// import com.apollographql.apollo.ApolloClient
// import com.apollographql.cache.normalized.FetchPolicy
// import com.apollographql.cache.normalized.fetchPolicy
// import com.hedvig.android.apollo.ErrorMessage
// import com.hedvig.android.apollo.safeExecute
// import com.hedvig.android.core.uidata.UiMoney
// import com.hedvig.android.logger.LogPriority
// import com.hedvig.android.logger.logcat
// import octopus.InsurableLimitsUsageQuery
// import octopus.fragment.InsurableLimitUsageFragment
