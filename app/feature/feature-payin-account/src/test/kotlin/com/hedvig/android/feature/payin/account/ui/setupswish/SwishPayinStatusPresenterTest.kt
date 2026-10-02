package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.runtime.mutableStateOf
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.payin.account.data.GetSwishPayinSetupStatusUseCase
import com.hedvig.android.feature.payin.account.data.SetupSwishPayinUseCase
import com.hedvig.android.feature.payin.account.data.SetupSwishResponse
import com.hedvig.android.feature.payin.account.data.SwishPayinSetupStatus
import com.hedvig.android.feature.payin.account.data.SwishSetupOrder
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SwishPayinStatusPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val firstOrder = SwishSetupOrder("https://swish/first", "firstOrderId")
  private val retriedOrder = SwishSetupOrder("https://swish/retried", "retriedOrderId")

  @Test
  fun `a fresh screen sets up an order and polls it until it is approved`() = runTest {
    val setupUseCase = FakeSetupSwishPayinUseCase()
    val statusUseCase = FakeGetSwishPayinSetupStatusUseCase()
    presenter(setupUseCase, statusUseCase, SavedStateHandle()).test(SwishPayinStatusUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Loading)
      setupUseCase.responses.send(pending(firstOrder).right())
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.PendingApproval(firstOrder.successUrl))
      statusUseCase.responses.send(SwishPayinSetupStatus.Active)
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Connected)
      assertThat(setupUseCase.calls).isEqualTo(1)
      assertThat(statusUseCase.polledOrderIds).containsExactly(firstOrder.orderId)
    }
  }

  @Test
  fun `after process death, keeps polling the saved order without setting up a new one`() = runTest {
    val setupUseCase = FakeSetupSwishPayinUseCase()
    val statusUseCase = FakeGetSwishPayinSetupStatusUseCase()
    // The saveable APIs only write into the handle on a real save-state pass, so the handle is
    // populated by hand with what an earlier setup would have left behind.
    val savedStateHandle = SavedStateHandle(
      mapOf("order" to bundleOf("value" to mutableStateOf(listOf(firstOrder.successUrl, firstOrder.orderId)))),
    )
    presenter(setupUseCase, statusUseCase, savedStateHandle).test(SwishPayinStatusUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Loading)
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.PendingApproval(firstOrder.successUrl))
      statusUseCase.responses.send(SwishPayinSetupStatus.Active)
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Connected)
      assertThat(setupUseCase.calls).isEqualTo(0)
      assertThat(statusUseCase.polledOrderIds).containsExactly(firstOrder.orderId)
    }
  }

  @Test
  fun `a failed setup can be retried into a new order`() = runTest {
    val setupUseCase = FakeSetupSwishPayinUseCase()
    val statusUseCase = FakeGetSwishPayinSetupStatusUseCase()
    presenter(setupUseCase, statusUseCase, SavedStateHandle()).test(SwishPayinStatusUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Loading)
      setupUseCase.responses.send(ErrorMessage("boom").left())
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Failed("boom"))
      sendEvent(SwishPayinStatusEvent.Retry)
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Failed("boom", isRetrying = true))
      setupUseCase.responses.send(pending(retriedOrder).right())
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.PendingApproval(retriedOrder.successUrl))
      statusUseCase.responses.send(SwishPayinSetupStatus.Active)
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Connected)
      assertThat(statusUseCase.polledOrderIds).containsExactly(retriedOrder.orderId)
    }
  }

  @Test
  fun `an order rejected in Swish is replaced by a new one on retry`() = runTest {
    val setupUseCase = FakeSetupSwishPayinUseCase()
    val statusUseCase = FakeGetSwishPayinSetupStatusUseCase()
    presenter(setupUseCase, statusUseCase, SavedStateHandle()).test(SwishPayinStatusUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Loading)
      setupUseCase.responses.send(pending(firstOrder).right())
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.PendingApproval(firstOrder.successUrl))
      statusUseCase.responses.send(SwishPayinSetupStatus.Failed("declined"))
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Failed("declined"))
      sendEvent(SwishPayinStatusEvent.Retry)
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Failed("declined", isRetrying = true))
      setupUseCase.responses.send(pending(retriedOrder).right())
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.PendingApproval(retriedOrder.successUrl))
      statusUseCase.responses.send(SwishPayinSetupStatus.Active)
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Connected)
      assertThat(statusUseCase.polledOrderIds).containsExactly(firstOrder.orderId, retriedOrder.orderId)
    }
  }

  @Test
  fun `a setup that needs no approving is connected straight away`() = runTest {
    val setupUseCase = FakeSetupSwishPayinUseCase()
    val statusUseCase = FakeGetSwishPayinSetupStatusUseCase()
    presenter(setupUseCase, statusUseCase, SavedStateHandle()).test(SwishPayinStatusUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Loading)
      setupUseCase.responses.send(SetupSwishResponse.Success(url = null, orderId = null).right())
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Connected)
      assertThat(statusUseCase.polledOrderIds).isEmpty()
    }
  }

  private fun pending(order: SwishSetupOrder) = SetupSwishResponse.Pending(order.successUrl, order.orderId)

  private fun presenter(
    setupUseCase: SetupSwishPayinUseCase,
    statusUseCase: GetSwishPayinSetupStatusUseCase,
    savedStateHandle: SavedStateHandle,
  ) = SwishPayinStatusPresenter(
    savedStateHandle = savedStateHandle,
    getSwishPayinSetupStatusUseCase = statusUseCase,
    setupSwishPayinUseCase = setupUseCase,
  )
}

private class FakeSetupSwishPayinUseCase : SetupSwishPayinUseCase {
  val responses = Channel<Either<ErrorMessage, SetupSwishResponse>>(Channel.UNLIMITED)
  var calls = 0

  override suspend fun invoke(phoneNumber: String): Either<ErrorMessage, SetupSwishResponse> {
    calls++
    return responses.receive()
  }
}

private class FakeGetSwishPayinSetupStatusUseCase : GetSwishPayinSetupStatusUseCase {
  val responses = Channel<SwishPayinSetupStatus>(Channel.UNLIMITED)
  val polledOrderIds = mutableListOf<String>()

  override suspend fun invoke(orderId: String): Either<ErrorMessage, SwishPayinSetupStatus> {
    polledOrderIds += orderId
    return responses.receive().right()
  }
}
