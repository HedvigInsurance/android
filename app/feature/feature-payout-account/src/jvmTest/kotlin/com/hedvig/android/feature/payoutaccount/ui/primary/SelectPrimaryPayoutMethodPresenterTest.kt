package com.hedvig.android.feature.payoutaccount.ui.primary

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.apollo.test.registerSuspendingTestNetworkError
import com.hedvig.android.apollo.test.registerSuspendingTestResponse
import com.hedvig.android.feature.payoutaccount.FakeNetworkCacheManager
import com.hedvig.android.feature.payoutaccount.bankPayoutMethod
import com.hedvig.android.feature.payoutaccount.data.GetPayoutAccountUseCase
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount
import com.hedvig.android.feature.payoutaccount.data.SetAsDefaultPayoutUseCase
import com.hedvig.android.feature.payoutaccount.payoutMethodsData
import com.hedvig.android.feature.payoutaccount.swishPayoutMethod
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import octopus.GetPayoutMethodsQuery
import octopus.SetAsDefaultPayoutMutation
import octopus.builder.Data
import octopus.builder.buildUserError
import octopus.type.MemberPaymentProvider
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class SelectPrimaryPayoutMethodPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.TURBINE_MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  private val bankAccount = PayoutAccount.BankAccount("3300", "9876543", "Nordea", isPending = false, isDefault = false)
  private val swish = PayoutAccount.SwishPayout("0701234567", isPending = false, isDefault = true)
  private val methods = listOf(bankAccount, swish)

  @Test
  fun `the member's methods are listed with nothing selected`() = runTest {
    presenter().test(SelectPrimaryPayoutMethodUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayoutMethodUiState.Loading)
      registerMethods()
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayoutMethodUiState.Content(methods, selectedMethod = null))
    }
  }

  @Test
  fun `confirming a selected method makes it primary and clears the cache`() = runTest {
    val cacheManager = FakeNetworkCacheManager()
    presenter(cacheManager).test(SelectPrimaryPayoutMethodUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayoutMethodUiState.Loading)
      registerMethods()
      val loaded = SelectPrimaryPayoutMethodUiState.Content(methods, selectedMethod = null)
      assertThat(awaitItem()).isEqualTo(loaded)

      sendEvent(SelectPrimaryPayoutMethodEvent.SelectMethod(bankAccount))
      assertThat(awaitItem()).isEqualTo(loaded.copy(selectedMethod = bankAccount))
      sendEvent(SelectPrimaryPayoutMethodEvent.ConfirmSelectedMethod)
      assertThat(awaitItem()).isEqualTo(loaded.copy(selectedMethod = bankAccount, isConfirming = true))
      registerSetAsDefault(MemberPaymentProvider.NORDEA, userError = null)
      assertThat(awaitItem()).isEqualTo(loaded.copy(selectedMethod = bankAccount, hasSetPrimaryMethod = true))
      assertThat(cacheManager.clears).isEqualTo(1)
    }
  }

  @Test
  fun `a rejected change shows the backend's message and leaves the primary method alone`() = runTest {
    val cacheManager = FakeNetworkCacheManager()
    presenter(cacheManager).test(SelectPrimaryPayoutMethodUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayoutMethodUiState.Loading)
      registerMethods()
      val loaded = SelectPrimaryPayoutMethodUiState.Content(methods, selectedMethod = null)
      assertThat(awaitItem()).isEqualTo(loaded)

      sendEvent(SelectPrimaryPayoutMethodEvent.SelectMethod(bankAccount))
      assertThat(awaitItem()).isEqualTo(loaded.copy(selectedMethod = bankAccount))
      sendEvent(SelectPrimaryPayoutMethodEvent.ConfirmSelectedMethod)
      assertThat(awaitItem()).isEqualTo(loaded.copy(selectedMethod = bankAccount, isConfirming = true))
      registerSetAsDefault(MemberPaymentProvider.NORDEA, userError = "Account is not verified")
      assertThat(awaitItem())
        .isEqualTo(loaded.copy(selectedMethod = bankAccount, errorMessage = "Account is not verified"))
      assertThat(cacheManager.clears).isEqualTo(0)
    }
  }

  @Test
  fun `confirming with nothing selected does nothing`() = runTest {
    presenter().test(SelectPrimaryPayoutMethodUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayoutMethodUiState.Loading)
      registerMethods()
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayoutMethodUiState.Content(methods, selectedMethod = null))

      sendEvent(SelectPrimaryPayoutMethodEvent.ConfirmSelectedMethod)
      this@runTest.testScheduler.advanceUntilIdle()
      expectNoEvents()
    }
  }

  @Test
  fun `a failed load can be retried`() = runTest {
    presenter().test(SelectPrimaryPayoutMethodUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayoutMethodUiState.Loading)
      apolloClient.registerSuspendingTestNetworkError(GetPayoutMethodsQuery())
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayoutMethodUiState.Failed)

      sendEvent(SelectPrimaryPayoutMethodEvent.Retry)
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayoutMethodUiState.Loading)
      registerMethods()
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayoutMethodUiState.Content(methods, selectedMethod = null))
    }
  }

  @Test
  fun `a restored selection is refreshed from the reloaded methods`() = runTest {
    val staleSwish = swish.copy(phoneNumber = "0700000000")
    val restored = SelectPrimaryPayoutMethodUiState.Content(listOf(staleSwish), selectedMethod = staleSwish)
    presenter().test(restored) {
      assertThat(awaitItem()).isEqualTo(restored)
      registerMethods()
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayoutMethodUiState.Content(methods, selectedMethod = swish))
    }
  }

  private fun presenter(cacheManager: FakeNetworkCacheManager = FakeNetworkCacheManager()) =
    SelectPrimaryPayoutMethodPresenter(
      getPayoutAccountUseCase = GetPayoutAccountUseCase(apolloClient),
      setAsDefaultPayoutUseCase = SetAsDefaultPayoutUseCase(apolloClient, cacheManager),
    )

  private fun registerMethods() {
    apolloClient.registerSuspendingTestResponse(
      GetPayoutMethodsQuery(),
      payoutMethodsData(
        payoutMethods = {
          listOf(
            swishPayoutMethod("0701234567", isDefault = true),
            bankPayoutMethod(MemberPaymentProvider.NORDEA, account = "3300-9876543", bank = "Nordea"),
          )
        },
      ),
    )
  }

  private fun registerSetAsDefault(provider: MemberPaymentProvider, userError: String?) {
    apolloClient.registerSuspendingTestResponse(
      SetAsDefaultPayoutMutation(provider),
      SetAsDefaultPayoutMutation.Data(OctopusFakeResolver) {
        paymentMethodSetDefaultPayout = userError?.let { buildUserError { message = it } }
      },
    )
  }
}
