package com.hedvig.android.feature.payin.account.data

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestNetworkError
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.paying.member.InvoiceDelivery
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.feature.payin.account.invoicePayinMethod
import com.hedvig.android.feature.payin.account.payinMethodsData
import com.hedvig.android.feature.payin.account.swishPayinMethod
import com.hedvig.android.feature.payin.account.trustlyPayinMethod
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.GetPayinMethodsQuery
import octopus.type.MemberPaymentMethodStatus
import octopus.type.MemberPaymentProvider
import octopus.type.PaymentMethodInvoiceDelivery
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetPayinAccountUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `each provider maps to its own payin account, in a stable display order`() = runTest {
    apolloClient.registerTestResponse(
      GetPayinMethodsQuery(),
      payinMethodsData(
        payinMethods = {
          listOf(
            trustlyPayinMethod(account = "8327-1234567", bank = "Swedbank", isDefault = true),
            swishPayinMethod("0701234567"),
            invoicePayinMethod(PaymentMethodInvoiceDelivery.KIVRA, email = null),
          )
        },
        chargingDay = 27,
      ),
    )

    assertThat(GetPayinAccountUseCase(apolloClient).invoke()).isEqualTo(
      Either.Right(
        PayinAccountData(
          currentMethods = listOf(
            PayinAccount.Invoice(InvoiceDelivery.Kivra, email = null, isPending = false, isDefault = false),
            PayinAccount.SwishPayin("0701234567", isPending = false, isDefault = false),
            PayinAccount.Trustly("8327", "1234567", "Swedbank", isPending = false, isDefault = true),
          ),
          availablePayinMethods = emptyList(),
          chargingDay = 27,
        ),
      ),
    )
  }

  @Test
  fun `a pending method is never shown as the default`() = runTest {
    apolloClient.registerTestResponse(
      GetPayinMethodsQuery(),
      payinMethodsData(
        payinMethods = {
          listOf(swishPayinMethod("0701234567", status = MemberPaymentMethodStatus.PENDING, isDefault = true))
        },
      ),
    )

    assertThat(GetPayinAccountUseCase(apolloClient).invoke().getOrNull()?.currentMethods).isEqualTo(
      listOf(PayinAccount.SwishPayin("0701234567", isPending = true, isDefault = false)),
    )
  }

  @Test
  fun `only available methods that support payin are offered`() = runTest {
    apolloClient.registerTestResponse(
      GetPayinMethodsQuery(),
      payinMethodsData(
        availablePayinProviders = listOf(MemberPaymentProvider.TRUSTLY, MemberPaymentProvider.SWISH),
        availablePayoutOnlyProviders = listOf(MemberPaymentProvider.NORDEA),
      ),
    )

    assertThat(GetPayinAccountUseCase(apolloClient).invoke().getOrNull()?.availablePayinMethods!!)
      .containsExactly(PaymentProvider.Trustly, PaymentProvider.Swish)
  }

  @Test
  fun `a member without a charging day has none`() = runTest {
    apolloClient.registerTestResponse(GetPayinMethodsQuery(), payinMethodsData(chargingDay = null))

    assertThat(GetPayinAccountUseCase(apolloClient).invoke().getOrNull()?.chargingDay).isNull()
  }

  @Test
  fun `a network failure is returned as an error`() = runTest {
    apolloClient.registerTestNetworkError(GetPayinMethodsQuery())

    assertThat(GetPayinAccountUseCase(apolloClient).invoke()).isInstanceOf<Either.Left<ErrorMessage>>()
  }
}
