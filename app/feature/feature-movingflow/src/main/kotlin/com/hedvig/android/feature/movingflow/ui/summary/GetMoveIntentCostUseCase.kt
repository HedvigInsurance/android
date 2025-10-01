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
              val moveIntentCost = response.bind().moveIntentCost
              val totalCost = moveIntentCost.totalCost
              val quoteCosts = moveIntentCost.quoteCosts
              MoveIntentCost(
                monthlyNet = UiMoney.fromMoneyFragment(totalCost.monthlyNet),
                monthlyGross = UiMoney.fromMoneyFragment(totalCost.monthlyGross),
                quoteCosts = quoteCosts.map { quoteCost ->
                  MoveIntentCost.QuoteCost(
                    id = quoteCost.quoteId,
                    monthlyNet = UiMoney.fromMoneyFragment(quoteCost.cost.monthlyNet),
                    monthlyGross = UiMoney.fromMoneyFragment(quoteCost.cost.monthlyGross),
                    discounts = quoteCost.cost.discounts.map { discount ->
                      MoveIntentCost.QuoteCost.Discount(
                        displayName = discount.displayName,
                        displayValue = discount.displayValue,
                        explanation = discount.explanation,
                        campaignCode = discount.campaignCode,
                      )
                    },
                  )
                },
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
  val quoteCosts: List<QuoteCost>,
) {
  data class QuoteCost(
    val id: String,
    val monthlyNet: UiMoney,
    val monthlyGross: UiMoney,
    val discounts: List<Discount>,
  ) {
    data class Discount(
      // Short name for list display, ex. "50% puppy discount"
      val displayName: String,
      // Short value for list display, ex. 50% 99 kr
      val displayValue: String,
      // Longer explanation, ex. 50% discount for 6 months
      val explanation: String,
      // Campaign code or some hardcoded descriptor of discount, ex. BUNDLE / BUYHEDVIG / etc
      val campaignCode: String,
    )
  }
}
