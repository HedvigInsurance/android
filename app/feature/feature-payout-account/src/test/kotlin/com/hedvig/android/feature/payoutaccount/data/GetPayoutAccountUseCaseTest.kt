package com.hedvig.android.feature.payoutaccount.data

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestNetworkError
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.feature.payoutaccount.bankPayoutMethod
import com.hedvig.android.feature.payoutaccount.payoutMethodsData
import com.hedvig.android.feature.payoutaccount.swishPayoutMethod
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.GetPayoutMethodsQuery
import octopus.builder.buildMemberPaymentMethod
import octopus.type.MemberPaymentMethodStatus
import octopus.type.MemberPaymentProvider
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetPayoutAccountUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `each provider maps to its own payout account, in a stable display order`() = runTest {
    apolloClient.registerTestResponse(
      GetPayoutMethodsQuery(),
      payoutMethodsData(
        payoutMethods = {
          listOf(
            bankPayoutMethod(MemberPaymentProvider.TRUSTLY, account = "8327-1234567", bank = "Swedbank"),
            swishPayoutMethod("0701234567", isDefault = true),
            bankPayoutMethod(MemberPaymentProvider.NORDEA, account = "3300-9876543", bank = "Nordea"),
          )
        },
      ),
    )

    assertThat(GetPayoutAccountUseCase(apolloClient).invoke()).isEqualTo(
      Either.Right(
        PayoutAccountData(
          currentMethods = listOf(
            PayoutAccount.BankAccount("3300", "9876543", "Nordea", isPending = false, isDefault = false),
            PayoutAccount.SwishPayout("0701234567", isPending = false, isDefault = true),
            PayoutAccount.Trustly("8327", "1234567", "Swedbank", isPending = false, isDefault = false),
          ),
          availablePayoutMethods = emptyList(),
        ),
      ),
    )
  }

  @Test
  fun `a pending method is never shown as the default`() = runTest {
    apolloClient.registerTestResponse(
      GetPayoutMethodsQuery(),
      payoutMethodsData(
        payoutMethods = {
          listOf(swishPayoutMethod("0701234567", status = MemberPaymentMethodStatus.PENDING, isDefault = true))
        },
      ),
    )

    assertThat(GetPayoutAccountUseCase(apolloClient).invoke().getOrNull()?.currentMethods).isEqualTo(
      listOf(PayoutAccount.SwishPayout("0701234567", isPending = true, isDefault = false)),
    )
  }

  @Test
  fun `an account number without a dash is all clearing number`() = runTest {
    apolloClient.registerTestResponse(
      GetPayoutMethodsQuery(),
      payoutMethodsData(
        payoutMethods = {
          listOf(bankPayoutMethod(MemberPaymentProvider.NORDEA, account = "33009876543", bank = "Nordea"))
        },
      ),
    )

    assertThat(GetPayoutAccountUseCase(apolloClient).invoke().getOrNull()?.currentMethods).isEqualTo(
      listOf(PayoutAccount.BankAccount("33009876543", null, "Nordea", isPending = false, isDefault = false)),
    )
  }

  @Test
  fun `methods from providers without a payout screen are dropped`() = runTest {
    apolloClient.registerTestResponse(
      GetPayoutMethodsQuery(),
      payoutMethodsData(
        payoutMethods = {
          listOf(
            buildMemberPaymentMethod {
              provider = MemberPaymentProvider.INVOICE
              status = MemberPaymentMethodStatus.ACTIVE
              isDefault = false
              details = null
            },
          )
        },
      ),
    )

    assertThat(GetPayoutAccountUseCase(apolloClient).invoke().getOrNull()?.currentMethods).isEqualTo(emptyList())
  }

  @Test
  fun `only available methods that support payout are offered`() = runTest {
    apolloClient.registerTestResponse(
      GetPayoutMethodsQuery(),
      payoutMethodsData(
        availablePayoutProviders = listOf(MemberPaymentProvider.SWISH, MemberPaymentProvider.NORDEA),
        availablePayinOnlyProviders = listOf(MemberPaymentProvider.INVOICE),
      ),
    )

    assertThat(GetPayoutAccountUseCase(apolloClient).invoke().getOrNull()?.availablePayoutMethods!!)
      .containsExactly(PaymentProvider.Swish, PaymentProvider.Nordea)
  }

  @Test
  fun `a network failure is returned as an error`() = runTest {
    apolloClient.registerTestNetworkError(GetPayoutMethodsQuery())

    assertThat(GetPayoutAccountUseCase(apolloClient).invoke()).isInstanceOf<Either.Left<ErrorMessage>>()
  }
}
