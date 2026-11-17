package com.hedvig.android.feature.payments.data

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.DataBuilderScope
import com.apollographql.apollo.testing.registerTestNetworkError
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.MemberCharge.MemberChargeStatus
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.ShortPaymentHistoryQuery
import octopus.builder.Data
import octopus.builder.MemberChargeMap
import octopus.builder.buildMember
import octopus.builder.buildMemberCharge
import octopus.builder.buildMoney
import octopus.type.CurrencyCode
import octopus.type.MemberChargeStatus as OctopusChargeStatus
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetPaymentsHistoryUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `past charges keep the backend's order, status and currency, and a missing id becomes empty`() = runTest {
    registerPastCharges {
      listOf(
        pastCharge("charge-4", OctopusChargeStatus.PENDING, LocalDate(2026, 4, 27), 399.0, CurrencyCode.SEK),
        pastCharge(null, OctopusChargeStatus.FAILED, LocalDate(2026, 3, 27), 449.5, CurrencyCode.SEK),
        pastCharge("charge-2", OctopusChargeStatus.SUCCESS, LocalDate(2025, 12, 27), 0.0, CurrencyCode.NOK),
        pastCharge("charge-5", OctopusChargeStatus.UPCOMING, LocalDate(2026, 5, 27), 120.25, CurrencyCode.DKK),
        pastCharge(null, OctopusChargeStatus.UNKNOWN__, LocalDate(2025, 1, 2), -10.0, CurrencyCode.SEK),
      )
    }

    assertThat(GetPaymentsHistoryUseCaseImpl(apolloClient).invoke()).isEqualTo(
      Either.Right(
        listOf(
          PaymentHistoryItem(
            UiMoney(399.0, UiCurrencyCode.SEK),
            "charge-4",
            MemberChargeStatus.PENDING,
            LocalDate(2026, 4, 27),
          ),
          PaymentHistoryItem(UiMoney(449.5, UiCurrencyCode.SEK), "", MemberChargeStatus.FAILED, LocalDate(2026, 3, 27)),
          PaymentHistoryItem(
            UiMoney(0.0, UiCurrencyCode.NOK),
            "charge-2",
            MemberChargeStatus.SUCCESS,
            LocalDate(2025, 12, 27),
          ),
          PaymentHistoryItem(
            UiMoney(120.25, UiCurrencyCode.DKK),
            "charge-5",
            MemberChargeStatus.UPCOMING,
            LocalDate(2026, 5, 27),
          ),
          PaymentHistoryItem(UiMoney(-10.0, UiCurrencyCode.SEK), "", MemberChargeStatus.UNKNOWN, LocalDate(2025, 1, 2)),
        ),
      ),
    )
  }

  @Test
  fun `a member without past charges has an empty history`() = runTest {
    registerPastCharges { emptyList() }

    assertThat(GetPaymentsHistoryUseCaseImpl(apolloClient).invoke()).isEqualTo(Either.Right(emptyList()))
  }

  @Test
  fun `a network failure fails`() = runTest {
    apolloClient.registerTestNetworkError(ShortPaymentHistoryQuery())

    assertThat(GetPaymentsHistoryUseCaseImpl(apolloClient).invoke()).isInstanceOf<Either.Left<ErrorMessage>>()
  }

  private fun registerPastCharges(pastCharges: DataBuilderScope.() -> List<MemberChargeMap>) {
    apolloClient.registerTestResponse(
      ShortPaymentHistoryQuery(),
      ShortPaymentHistoryQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember { this.pastCharges = pastCharges() }
      },
    )
  }

  private fun DataBuilderScope.pastCharge(
    id: String?,
    status: OctopusChargeStatus,
    date: LocalDate,
    amount: Double,
    currencyCode: CurrencyCode,
  ) = buildMemberCharge {
    this.id = id
    this.status = status
    this.date = date
    net = buildMoney {
      this.amount = amount
      this.currencyCode = currencyCode
    }
  }
}
