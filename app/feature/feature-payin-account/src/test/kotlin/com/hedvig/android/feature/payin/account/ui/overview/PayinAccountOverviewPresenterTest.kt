package com.hedvig.android.feature.payin.account.ui.overview

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.apollo.test.registerSuspendingTestNetworkError
import com.hedvig.android.apollo.test.registerSuspendingTestResponse
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.feature.payin.account.data.GetPayinAccountUseCase
import com.hedvig.android.feature.payin.account.payinMethodsData
import com.hedvig.android.feature.payin.account.swishPayinMethod
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import octopus.GetPayinMethodsQuery
import octopus.type.MemberPaymentProvider
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class PayinAccountOverviewPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.TURBINE_MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  private val swish = PayinAccount.SwishPayin("0701234567", isPending = false, isDefault = true)

  @Test
  fun `connected and available methods are shown`() = runTest {
    presenter().test(PayinAccountOverviewUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayinAccountOverviewUiState.Loading)
      registerMethods(withSwish = true, available = listOf(MemberPaymentProvider.TRUSTLY))
      assertThat(awaitItem())
        .isEqualTo(PayinAccountOverviewUiState.Content(listOf(swish), listOf(PaymentProvider.Trustly)))
    }
  }

  @Test
  fun `a failed load shows an error, and retrying loads again`() = runTest {
    presenter().test(PayinAccountOverviewUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayinAccountOverviewUiState.Loading)
      apolloClient.registerSuspendingTestNetworkError(GetPayinMethodsQuery())
      assertThat(awaitItem()).isEqualTo(PayinAccountOverviewUiState.Error)
      sendEvent(PayinAccountOverviewEvent.Retry)
      assertThat(awaitItem()).isEqualTo(PayinAccountOverviewUiState.Loading)
      registerMethods(withSwish = true, available = emptyList())
      assertThat(awaitItem()).isEqualTo(PayinAccountOverviewUiState.Content(listOf(swish), emptyList()))
    }
  }

  @Test
  fun `a refresh updates the list in place`() = runTest {
    presenter().test(PayinAccountOverviewUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayinAccountOverviewUiState.Loading)
      registerMethods(withSwish = false, available = listOf(MemberPaymentProvider.SWISH))
      assertThat(awaitItem())
        .isEqualTo(PayinAccountOverviewUiState.Content(emptyList(), listOf(PaymentProvider.Swish)))

      sendEvent(PayinAccountOverviewEvent.Refresh)
      registerMethods(withSwish = true, available = emptyList())
      assertThat(awaitItem()).isEqualTo(PayinAccountOverviewUiState.Content(listOf(swish), emptyList()))
    }
  }

  @Test
  fun `a refresh that fails keeps the list on screen`() = runTest {
    presenter().test(PayinAccountOverviewUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayinAccountOverviewUiState.Loading)
      registerMethods(withSwish = true, available = emptyList())
      val content = PayinAccountOverviewUiState.Content(listOf(swish), emptyList())
      assertThat(awaitItem()).isEqualTo(content)

      sendEvent(PayinAccountOverviewEvent.Refresh)
      apolloClient.registerSuspendingTestNetworkError(GetPayinMethodsQuery())
      this@runTest.testScheduler.advanceUntilIdle()
      assertThat(expectMostRecentItem()).isEqualTo(content)
    }
  }

  private fun presenter() = PayinAccountOverviewPresenter(GetPayinAccountUseCase(apolloClient))

  private fun registerMethods(withSwish: Boolean, available: List<MemberPaymentProvider>) {
    apolloClient.registerSuspendingTestResponse(
      GetPayinMethodsQuery(),
      payinMethodsData(
        payinMethods = { if (withSwish) listOf(swishPayinMethod("0701234567", isDefault = true)) else emptyList() },
        availablePayinProviders = available,
      ),
    )
  }
}
