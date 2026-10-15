package com.hedvig.android.feature.payin.account.ui.primary

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.apollo.test.registerSuspendingTestNetworkError
import com.hedvig.android.apollo.test.registerSuspendingTestResponse
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.feature.payin.account.data.GetPayinAccountUseCase
import com.hedvig.android.feature.payin.account.data.PayinAccountData
import com.hedvig.android.feature.payin.account.data.SetAsDefaultUseCase
import com.hedvig.android.feature.payin.account.payinMethodsData
import com.hedvig.android.feature.payin.account.swishPayinMethod
import com.hedvig.android.feature.payin.account.trustlyPayinMethod
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import octopus.GetPayinMethodsQuery
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class SelectPrimaryPayinMethodPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.TURBINE_MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  private val swish = PayinAccount.SwishPayin("0701234567", isPending = false, isDefault = false)
  private val trustly = PayinAccount.Trustly("8327", "1234567", "Swedbank", isPending = false, isDefault = true)
  private val methods = listOf(swish, trustly)
  private val loaded = SelectPrimaryPayinMethodUiState.Content(methods, selectedMethod = null)

  @Test
  fun `confirming a selected method makes it primary`() = runTest {
    val setAsDefault = FakeSetAsDefaultUseCase()
    presenter(setAsDefault).test(SelectPrimaryPayinMethodUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayinMethodUiState.Loading)
      registerMethods()
      assertThat(awaitItem()).isEqualTo(loaded)

      sendEvent(SelectPrimaryPayinMethodEvent.SelectMethod(swish))
      assertThat(awaitItem()).isEqualTo(loaded.copy(selectedMethod = swish))
      sendEvent(SelectPrimaryPayinMethodEvent.ConfirmSelectedMethod)
      assertThat(awaitItem()).isEqualTo(loaded.copy(selectedMethod = swish, isConfirming = true))
      setAsDefault.responses.send(PayinAccountData(methods, emptyList(), chargingDay = 27).right())
      assertThat(awaitItem()).isEqualTo(loaded.copy(selectedMethod = swish, hasSetPrimaryMethod = true))
      assertThat(setAsDefault.providers).containsExactly(PaymentProvider.Swish)
    }
  }

  @Test
  fun `a rejected change shows the backend's message`() = runTest {
    val setAsDefault = FakeSetAsDefaultUseCase()
    presenter(setAsDefault).test(SelectPrimaryPayinMethodUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayinMethodUiState.Loading)
      registerMethods()
      assertThat(awaitItem()).isEqualTo(loaded)

      sendEvent(SelectPrimaryPayinMethodEvent.SelectMethod(swish))
      assertThat(awaitItem()).isEqualTo(loaded.copy(selectedMethod = swish))
      sendEvent(SelectPrimaryPayinMethodEvent.ConfirmSelectedMethod)
      assertThat(awaitItem()).isEqualTo(loaded.copy(selectedMethod = swish, isConfirming = true))
      setAsDefault.responses.send(ErrorMessage("Method is not active yet").left())
      assertThat(awaitItem())
        .isEqualTo(loaded.copy(selectedMethod = swish, errorMessage = "Method is not active yet"))
    }
  }

  @Test
  fun `confirming with nothing selected does nothing`() = runTest {
    val setAsDefault = FakeSetAsDefaultUseCase()
    presenter(setAsDefault).test(SelectPrimaryPayinMethodUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayinMethodUiState.Loading)
      registerMethods()
      assertThat(awaitItem()).isEqualTo(loaded)

      sendEvent(SelectPrimaryPayinMethodEvent.ConfirmSelectedMethod)
      this@runTest.testScheduler.advanceUntilIdle()
      assertThat(setAsDefault.providers).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a failed load can be retried`() = runTest {
    presenter().test(SelectPrimaryPayinMethodUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayinMethodUiState.Loading)
      apolloClient.registerSuspendingTestNetworkError(GetPayinMethodsQuery())
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayinMethodUiState.Failed)
      sendEvent(SelectPrimaryPayinMethodEvent.Retry)
      assertThat(awaitItem()).isEqualTo(SelectPrimaryPayinMethodUiState.Loading)
      registerMethods()
      assertThat(awaitItem()).isEqualTo(loaded)
    }
  }

  @Test
  fun `a refetch that fails keeps the restored list on screen`() = runTest {
    val restored = loaded.copy(selectedMethod = swish)
    presenter().test(restored) {
      assertThat(awaitItem()).isEqualTo(restored)
      apolloClient.registerSuspendingTestNetworkError(GetPayinMethodsQuery())
      this@runTest.testScheduler.advanceUntilIdle()
      expectNoEvents()
    }
  }

  private fun presenter(setAsDefault: SetAsDefaultUseCase = FakeSetAsDefaultUseCase()) =
    SelectPrimaryPayinMethodPresenter(GetPayinAccountUseCase(apolloClient), setAsDefault)

  private fun registerMethods() {
    apolloClient.registerSuspendingTestResponse(
      GetPayinMethodsQuery(),
      payinMethodsData(
        payinMethods = {
          listOf(
            swishPayinMethod("0701234567"),
            trustlyPayinMethod(account = "8327-1234567", bank = "Swedbank", isDefault = true),
          )
        },
      ),
    )
  }
}

private class FakeSetAsDefaultUseCase : SetAsDefaultUseCase {
  val responses = Channel<Either<ErrorMessage, PayinAccountData>>(Channel.UNLIMITED)
  val providers = mutableListOf<PaymentProvider>()

  override suspend fun invoke(provider: PaymentProvider): Either<ErrorMessage, PayinAccountData> {
    providers += provider
    return responses.receive()
  }
}
