package com.hedvig.android.feature.payin.account.ui.methoddetails

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
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
import com.hedvig.android.feature.payin.account.data.RemoveMethodUseCase
import com.hedvig.android.feature.payin.account.navigation.PayinMethodId
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
class PayinMethodDetailsPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.TURBINE_MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  private val swish = PayinAccount.SwishPayin("0701234567", isPending = false, isDefault = false)
  private val loaded = PayinMethodDetailsUiState.Content(swish, chargingDay = 27)

  @Test
  fun `the requested method is shown with the member's charging day`() = runTest {
    presenter(PayinMethodId.Swish).test(PayinMethodDetailsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayinMethodDetailsUiState.Loading)
      registerMethods()
      assertThat(awaitItem()).isEqualTo(loaded)
    }
  }

  @Test
  fun `a method the member no longer has is an error`() = runTest {
    presenter(PayinMethodId.Invoice).test(PayinMethodDetailsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayinMethodDetailsUiState.Loading)
      registerMethods()
      assertThat(awaitItem()).isEqualTo(PayinMethodDetailsUiState.Error)
    }
  }

  @Test
  fun `a failed load can be retried`() = runTest {
    presenter(PayinMethodId.Swish).test(PayinMethodDetailsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayinMethodDetailsUiState.Loading)
      apolloClient.registerSuspendingTestNetworkError(GetPayinMethodsQuery())
      assertThat(awaitItem()).isEqualTo(PayinMethodDetailsUiState.Error)
      sendEvent(PayinMethodDetailsEvent.Retry)
      assertThat(awaitItem()).isEqualTo(PayinMethodDetailsUiState.Loading)
      registerMethods()
      assertThat(awaitItem()).isEqualTo(loaded)
    }
  }

  @Test
  fun `removing the method marks it removed`() = runTest {
    val remove = FakeRemoveMethodUseCase()
    presenter(PayinMethodId.Swish, remove).test(PayinMethodDetailsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayinMethodDetailsUiState.Loading)
      registerMethods()
      assertThat(awaitItem()).isEqualTo(loaded)

      sendEvent(PayinMethodDetailsEvent.RemoveMethod)
      assertThat(awaitItem()).isEqualTo(loaded.copy(isRemoving = true))
      remove.responses.send(Unit.right())
      assertThat(awaitItem()).isEqualTo(loaded.copy(isRemoving = true, hasRemovedMethod = true))
      assertThat(remove.removedProviders).containsExactly(PaymentProvider.Swish)
    }
  }

  @Test
  fun `a failed removal shows the error and can be tried again`() = runTest {
    val remove = FakeRemoveMethodUseCase()
    val rejected = ErrorMessage("Primary method cannot be removed")
    presenter(PayinMethodId.Swish, remove).test(PayinMethodDetailsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayinMethodDetailsUiState.Loading)
      registerMethods()
      assertThat(awaitItem()).isEqualTo(loaded)

      sendEvent(PayinMethodDetailsEvent.RemoveMethod)
      assertThat(awaitItem()).isEqualTo(loaded.copy(isRemoving = true))
      remove.responses.send(rejected.left())
      assertThat(awaitItem()).isEqualTo(loaded.copy(removeError = rejected))

      sendEvent(PayinMethodDetailsEvent.RemoveMethod)
      assertThat(awaitItem()).isEqualTo(loaded.copy(isRemoving = true))
      assertThat(remove.removedProviders).containsExactly(PaymentProvider.Swish, PaymentProvider.Swish)
    }
  }

  @Test
  fun `a second remove while one is in flight is ignored`() = runTest {
    val remove = FakeRemoveMethodUseCase()
    presenter(PayinMethodId.Swish, remove).test(PayinMethodDetailsUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(PayinMethodDetailsUiState.Loading)
      registerMethods()
      assertThat(awaitItem()).isEqualTo(loaded)

      sendEvent(PayinMethodDetailsEvent.RemoveMethod)
      assertThat(awaitItem()).isEqualTo(loaded.copy(isRemoving = true))
      sendEvent(PayinMethodDetailsEvent.RemoveMethod)
      this@runTest.testScheduler.advanceUntilIdle()
      assertThat(remove.removedProviders).containsExactly(PaymentProvider.Swish)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun presenter(methodId: PayinMethodId, remove: RemoveMethodUseCase = FakeRemoveMethodUseCase()) =
    PayinMethodDetailsPresenter(methodId, GetPayinAccountUseCase(apolloClient), remove)

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
        chargingDay = 27,
      ),
    )
  }
}

private class FakeRemoveMethodUseCase : RemoveMethodUseCase {
  val responses = Channel<Either<ErrorMessage, Unit>>(Channel.UNLIMITED)
  val removedProviders = mutableListOf<PaymentProvider>()

  override suspend fun invoke(provider: PaymentProvider): Either<ErrorMessage, Unit> {
    removedProviders += provider
    return responses.receive()
  }
}
