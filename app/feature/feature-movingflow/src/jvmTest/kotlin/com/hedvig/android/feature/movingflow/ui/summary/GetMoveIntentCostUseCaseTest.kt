package com.hedvig.android.feature.movingflow.ui.summary

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.enqueueTestNetworkError
import com.apollographql.apollo.testing.enqueueTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.builder.Data
import octopus.builder.buildItemCost
import octopus.builder.buildItemDiscount
import octopus.builder.buildMoney
import octopus.builder.buildMoveIntentCost
import octopus.builder.buildMoveIntentQuoteCost
import octopus.builder.buildMoveIntentTotalCost
import octopus.feature.movingflow.MoveIntentCostQuery
import octopus.type.CurrencyCode
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetMoveIntentCostUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule()

  private val query = MoveIntentCostQuery("intentId", "quoteId", listOf("addonId"))

  @Test
  fun `the cost response is mapped`() = runTest {
    testApolloClientRule.apolloClient.enqueueTestResponse(query, testMoveIntentCostData(quoteId = "quoteId"))
    val useCase = GetMoveIntentCostUseCase(testApolloClientRule.apolloClient)

    useCase.invoke("intentId", "quoteId", listOf("addonId")).test {
      assertThat(awaitItem()).isRight().isEqualTo(
        MoveIntentCost(
          monthlyNet = UiMoney(250.0, UiCurrencyCode.SEK),
          monthlyGross = UiMoney(300.0, UiCurrencyCode.SEK),
          quoteCosts = listOf(
            MoveIntentCost.QuoteCost(
              id = "quoteId",
              monthlyNet = UiMoney(170.0, UiCurrencyCode.SEK),
              monthlyGross = UiMoney(200.0, UiCurrencyCode.SEK),
              discounts = listOf(
                MoveIntentCost.QuoteCost.Discount(
                  displayName = "Bundle discount",
                  displayValue = "-30 kr",
                  explanation = "Discount for having several insurances",
                  campaignCode = "BUNDLE",
                ),
              ),
            ),
          ),
        ),
      )
      awaitComplete()
    }
  }

  @Test
  fun `a failure is emitted and then retried until it succeeds`() = runTest {
    testApolloClientRule.apolloClient.enqueueTestNetworkError()
    testApolloClientRule.apolloClient.enqueueTestNetworkError()
    testApolloClientRule.apolloClient.enqueueTestResponse(query, testMoveIntentCostData(quoteId = "quoteId"))
    val useCase = GetMoveIntentCostUseCase(testApolloClientRule.apolloClient)

    useCase.invoke("intentId", "quoteId", listOf("addonId")).test {
      assertThat(awaitItem()).isLeft()
      assertThat(awaitItem()).isLeft()
      assertThat(awaitItem()).isRight()
      awaitComplete()
    }
  }
}

internal fun testMoveIntentCostData(quoteId: String): MoveIntentCostQuery.Data = MoveIntentCostQuery.Data(
  OctopusFakeResolver,
) {
  moveIntentCost = buildMoveIntentCost {
    totalCost = buildMoveIntentTotalCost {
      monthlyNet = buildMoney {
        amount = 250.0
        currencyCode = CurrencyCode.SEK
      }
      monthlyGross = buildMoney {
        amount = 300.0
        currencyCode = CurrencyCode.SEK
      }
    }
    quoteCosts = listOf(
      buildMoveIntentQuoteCost {
        this.quoteId = quoteId
        cost = buildItemCost {
          monthlyNet = buildMoney {
            amount = 170.0
            currencyCode = CurrencyCode.SEK
          }
          monthlyGross = buildMoney {
            amount = 200.0
            currencyCode = CurrencyCode.SEK
          }
          discounts = listOf(
            buildItemDiscount {
              displayName = "Bundle discount"
              displayValue = "-30 kr"
              explanation = "Discount for having several insurances"
              campaignCode = "BUNDLE"
            },
          )
        }
      },
    )
  }
}
