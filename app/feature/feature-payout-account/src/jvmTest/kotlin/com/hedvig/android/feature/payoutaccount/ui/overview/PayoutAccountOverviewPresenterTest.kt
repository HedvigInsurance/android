package com.hedvig.android.feature.payoutaccount.ui.overview

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.apollo.test.registerSuspendingTestNetworkError
import com.hedvig.android.apollo.test.registerSuspendingTestResponse
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.feature.payoutaccount.data.GetPayoutAccountUseCase
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount
import com.hedvig.android.feature.payoutaccount.payoutMethodsData
import com.hedvig.android.feature.payoutaccount.swishPayoutMethod
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import octopus.GetPayoutMethodsQuery
import octopus.type.MemberPaymentProvider
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class PayoutAccountOverviewPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.TURBINE_MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  private val swish = PayoutAccount.SwishPayout("0701234567", isPending = false, isDefault = true)

  @Test
  fun `connected and available methods are shown`() = runTest {
    presenter().test(PayoutAccountOverviewUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayoutAccountOverviewUiState.Loading)
      registerMethods(withSwish = true, available = listOf(MemberPaymentProvider.NORDEA))
      assertThat(awaitItem()).isEqualTo(
        PayoutAccountOverviewUiState.Content(listOf(swish), listOf(PaymentProvider.Nordea)),
      )
    }
  }

  @Test
  fun `a member with nothing connected and nothing to connect has no payout options`() = runTest {
    presenter().test(PayoutAccountOverviewUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayoutAccountOverviewUiState.Loading)
      registerMethods(withSwish = false, available = emptyList())
      assertThat(awaitItem()).isEqualTo(PayoutAccountOverviewUiState.NoPayoutOptions)
    }
  }

  @Test
  fun `a failed load shows an error, and retrying loads again`() = runTest {
    presenter().test(PayoutAccountOverviewUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayoutAccountOverviewUiState.Loading)
      apolloClient.registerSuspendingTestNetworkError(GetPayoutMethodsQuery())
      assertThat(awaitItem()).isEqualTo(PayoutAccountOverviewUiState.Error)
      sendEvent(PayoutAccountOverviewEvent.Retry)
      assertThat(awaitItem()).isEqualTo(PayoutAccountOverviewUiState.Loading)
      registerMethods(withSwish = true, available = emptyList())
      assertThat(awaitItem()).isEqualTo(PayoutAccountOverviewUiState.Content(listOf(swish), emptyList()))
    }
  }

  @Test
  fun `a refresh updates the list in place`() = runTest {
    presenter().test(PayoutAccountOverviewUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayoutAccountOverviewUiState.Loading)
      registerMethods(withSwish = false, available = listOf(MemberPaymentProvider.SWISH))
      assertThat(awaitItem())
        .isEqualTo(PayoutAccountOverviewUiState.Content(emptyList(), listOf(PaymentProvider.Swish)))

      sendEvent(PayoutAccountOverviewEvent.Refresh)
      registerMethods(withSwish = true, available = emptyList())
      assertThat(awaitItem()).isEqualTo(PayoutAccountOverviewUiState.Content(listOf(swish), emptyList()))
    }
  }

  @Test
  fun `a refresh that fails keeps the list on screen`() = runTest {
    presenter().test(PayoutAccountOverviewUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayoutAccountOverviewUiState.Loading)
      registerMethods(withSwish = true, available = emptyList())
      assertThat(awaitItem()).isEqualTo(PayoutAccountOverviewUiState.Content(listOf(swish), emptyList()))

      sendEvent(PayoutAccountOverviewEvent.Refresh)
      apolloClient.registerSuspendingTestNetworkError(GetPayoutMethodsQuery())
      this@runTest.testScheduler.advanceUntilIdle()
      assertThat(expectMostRecentItem())
        .isEqualTo(PayoutAccountOverviewUiState.Content(listOf(swish), emptyList()))
    }
  }

  private fun presenter() = PayoutAccountOverviewPresenter(GetPayoutAccountUseCase(apolloClient))

  private fun registerMethods(withSwish: Boolean, available: List<MemberPaymentProvider>) {
    apolloClient.registerSuspendingTestResponse(
      GetPayoutMethodsQuery(),
      payoutMethodsData(
        payoutMethods = { if (withSwish) listOf(swishPayoutMethod("0701234567", isDefault = true)) else emptyList() },
        availablePayoutProviders = available,
      ),
    )
  }
}
