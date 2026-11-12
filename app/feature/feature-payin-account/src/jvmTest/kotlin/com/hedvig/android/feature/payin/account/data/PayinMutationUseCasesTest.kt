package com.hedvig.android.feature.payin.account.data

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestNetworkError
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.feature.payin.account.FakeNetworkCacheManager
import com.hedvig.android.feature.payin.account.payinMethodsData
import com.hedvig.android.feature.payin.account.swishPayinMethod
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.GetPayinMethodsQuery
import octopus.RemovePayinMethodMutation
import octopus.SetAsDefaultPayinMutation
import octopus.builder.Data
import octopus.builder.buildUserError
import octopus.type.MemberPaymentProvider
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class PayinMutationUseCasesTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `removing a method clears the cache`() = runTest {
    val cacheManager = FakeNetworkCacheManager()
    registerRemove(userError = null)

    val result = RemoveMethodUseCaseImpl(apolloClient, cacheManager).invoke(PaymentProvider.Swish)

    assertThat(result).isEqualTo(Either.Right(Unit))
    assertThat(cacheManager.clears).isEqualTo(1)
  }

  @Test
  fun `a removal the backend rejects returns its message and still clears the cache`() = runTest {
    val cacheManager = FakeNetworkCacheManager()
    registerRemove(userError = "Primary method cannot be removed")

    val result = RemoveMethodUseCaseImpl(apolloClient, cacheManager).invoke(PaymentProvider.Swish)

    assertThat((result as Either.Left).value.message).isEqualTo("Primary method cannot be removed")
    assertThat(cacheManager.clears).isEqualTo(1)
  }

  @Test
  fun `a removal that cannot reach the backend still clears the cache`() = runTest {
    val cacheManager = FakeNetworkCacheManager()
    apolloClient.registerTestNetworkError(RemovePayinMethodMutation(MemberPaymentProvider.SWISH))

    val result = RemoveMethodUseCaseImpl(apolloClient, cacheManager).invoke(PaymentProvider.Swish)

    assertThat(result).isInstanceOf<Either.Left<ErrorMessage>>()
    assertThat(cacheManager.clears).isEqualTo(1)
  }

  @Test
  fun `making a method primary returns the refetched methods`() = runTest {
    registerSetAsDefault(userError = null)
    apolloClient.registerTestResponse(
      GetPayinMethodsQuery(),
      payinMethodsData(payinMethods = { listOf(swishPayinMethod("0701234567", isDefault = true)) }),
    )

    val result = setAsDefaultUseCase().invoke(PaymentProvider.Swish)

    assertThat(result.getOrNull()?.currentMethods).isEqualTo(
      listOf(PayinAccount.SwishPayin("0701234567", isPending = false, isDefault = true)),
    )
  }

  @Test
  fun `a primary change the backend rejects returns its message`() = runTest {
    registerSetAsDefault(userError = "Method is not active yet")

    val result = setAsDefaultUseCase().invoke(PaymentProvider.Swish)

    assertThat((result as Either.Left).value.message).isEqualTo("Method is not active yet")
  }

  private fun setAsDefaultUseCase() = SetAsDefaultUseCaseImpl(apolloClient, GetPayinAccountUseCase(apolloClient))

  private fun registerRemove(userError: String?) {
    apolloClient.registerTestResponse(
      RemovePayinMethodMutation(MemberPaymentProvider.SWISH),
      RemovePayinMethodMutation.Data(OctopusFakeResolver) {
        paymentMethodRemoveMethod = userError?.let { buildUserError { message = it } }
      },
    )
  }

  private fun registerSetAsDefault(userError: String?) {
    apolloClient.registerTestResponse(
      SetAsDefaultPayinMutation(MemberPaymentProvider.SWISH),
      SetAsDefaultPayinMutation.Data(OctopusFakeResolver) {
        paymentMethodSetDefaultPayin = userError?.let { buildUserError { message = it } }
      },
    )
  }
}
