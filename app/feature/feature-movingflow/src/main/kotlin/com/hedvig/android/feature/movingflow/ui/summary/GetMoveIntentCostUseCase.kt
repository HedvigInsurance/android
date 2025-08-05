package com.hedvig.android.feature.movingflow.ui.summary

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import octopus.feature.movingflow.MoveIntentCostQuery

internal class GetMoveIntentCostUseCase(
  private val apolloClient: ApolloClient,
) {
  fun invoke(
    intentId: String,
    selectedHomeQuoteId: String,
    selectedAddonIds: List<String>,
  ): Flow<Either<ErrorMessage, MoveIntentCost>> {
    return flow {
      var failedTries = 0
      do {
        apolloClient
          .query(MoveIntentCostQuery(intentId, selectedHomeQuoteId, selectedAddonIds))
          .fetchPolicy(FetchPolicy.CacheAndNetwork)
          .safeFlow(::ErrorMessage)
          .map { response ->
            either {
              val totalCost = response.bind().moveIntentCost.totalCost
              MoveIntentCost(
                monthlyNet = UiMoney.fromMoneyFragment(totalCost.monthlyNet),
                monthlyGross = UiMoney.fromMoneyFragment(totalCost.monthlyGross),
              )
            }
          }
          .collect { result ->
            when (result) {
              is Either.Left<*> -> {
                failedTries++
                logcat(
                  priority = if (failedTries > 5) {
                    LogPriority.ERROR
                  } else {
                    LogPriority.INFO
                  },
                ) { "Failed to get move intent cost: $result failedTries: $failedTries" }
              }

              is Either.Right<*> -> {
                failedTries = 0
              }
            }
            emit(result)
          }
        delay(failedTries.coerceAtMost(5).seconds)
      } while (failedTries > 0)
    }
  }
}

internal data class MoveIntentCost(
  val monthlyNet: UiMoney,
  val monthlyGross: UiMoney,
)
