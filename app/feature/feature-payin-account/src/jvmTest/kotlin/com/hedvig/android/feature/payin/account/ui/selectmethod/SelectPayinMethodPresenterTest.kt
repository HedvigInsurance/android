package com.hedvig.android.feature.payin.account.ui.selectmethod

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.apollo.test.registerSuspendingTestNetworkError
import com.hedvig.android.apollo.test.registerSuspendingTestResponse
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.feature.payin.account.data.GetPayinAccountUseCase
import com.hedvig.android.feature.payin.account.payinMethodsData
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import octopus.GetPayinMethodsQuery
import octopus.type.MemberPaymentProvider
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class SelectPayinMethodPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.TURBINE_MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  private val loaded = SelectPayinMethodUiState.Content(listOf(PaymentProvider.Trustly, PaymentProvider.Swish))

  @Test
  fun `the providers the member can connect are offered, and one can be picked`() = runTest {
    presenter().test(SelectPayinMethodUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SelectPayinMethodUiState.Loading)
      registerAvailable()
      assertThat(awaitItem()).isEqualTo(loaded)
      sendEvent(SelectPayinMethodEvent.SelectProvider(PaymentProvider.Swish))
      assertThat(awaitItem()).isEqualTo(loaded.copy(selectedProvider = PaymentProvider.Swish))
    }
  }

  @Test
  fun `a failed load can be retried`() = runTest {
    presenter().test(SelectPayinMethodUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SelectPayinMethodUiState.Loading)
      apolloClient.registerSuspendingTestNetworkError(GetPayinMethodsQuery())
      assertThat(awaitItem()).isEqualTo(SelectPayinMethodUiState.Error)
      sendEvent(SelectPayinMethodEvent.Retry)
      assertThat(awaitItem()).isEqualTo(SelectPayinMethodUiState.Loading)
      registerAvailable()
      assertThat(awaitItem()).isEqualTo(loaded)
    }
  }

  @Test
  fun `a restored screen keeps its selection without loading again`() = runTest {
    val restored = loaded.copy(selectedProvider = PaymentProvider.Trustly)
    presenter().test(restored) {
      assertThat(awaitItem()).isEqualTo(restored)
      this@runTest.testScheduler.advanceUntilIdle()
      expectNoEvents()
    }
  }

  private fun presenter() = SelectPayinMethodPresenter(GetPayinAccountUseCase(apolloClient))

  private fun registerAvailable() {
    apolloClient.registerSuspendingTestResponse(
      GetPayinMethodsQuery(),
      payinMethodsData(
        availablePayinProviders = listOf(MemberPaymentProvider.TRUSTLY, MemberPaymentProvider.SWISH),
        availablePayoutOnlyProviders = listOf(MemberPaymentProvider.NORDEA),
      ),
    )
  }
}
