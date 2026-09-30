package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.runtime.mutableStateOf
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
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

  private val initialOrder = SwishSetupOrder("https://swish/initial", "initialOrderId")
  private val retriedOrder = SwishSetupOrder("https://swish/retried", "retriedOrderId")

  @Test
  fun `a fresh order is handed over once, and not again after the member opened Swish`() = runTest {
    val statusUseCase = FakeGetSwishPayinSetupStatusUseCase()
    presenter(statusUseCase, SavedStateHandle()).test(initialViewModelState()) {
      assertThat(awaitItem()).isEqualTo(initialViewModelState())
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.PendingApproval(initialOrder.successUrl, true))
      sendEvent(SwishPayinStatusEvent.DidOpenSwishApp)
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.PendingApproval(initialOrder.successUrl, false))
      statusUseCase.responses.send(SwishPayinSetupStatus.Active)
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Connected)
      assertThat(statusUseCase.polledOrderIds).containsExactly(initialOrder.orderId)
    }
  }

  @Test
  fun `after process death, keeps polling the retried order without handing over again`() = runTest {
    val statusUseCase = FakeGetSwishPayinSetupStatusUseCase()
    // The saveable APIs only write into the handle on a real save-state pass, so the handle is
    // populated by hand with what a retry followed by a handover would have left behind.
    val savedStateHandle = SavedStateHandle(
      mapOf(
        "order" to bundleOf("value" to mutableStateOf(listOf(retriedOrder.successUrl, retriedOrder.orderId))),
        "handedOverOrderId" to bundleOf("value" to mutableStateOf(retriedOrder.orderId)),
      ),
    )
    presenter(statusUseCase, savedStateHandle).test(initialViewModelState()) {
      assertThat(awaitItem()).isEqualTo(initialViewModelState())
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.PendingApproval(retriedOrder.successUrl, false))
      statusUseCase.responses.send(SwishPayinSetupStatus.Active)
      assertThat(awaitItem()).isEqualTo(SwishPayinStatusUiState.Connected)
      assertThat(statusUseCase.polledOrderIds).containsExactly(retriedOrder.orderId)
    }
  }

  private fun initialViewModelState() = SwishPayinStatusUiState.PendingApproval(initialOrder.successUrl, false)

  private fun presenter(statusUseCase: GetSwishPayinSetupStatusUseCase, savedStateHandle: SavedStateHandle) =
    SwishPayinStatusPresenter(
      initialOrder = initialOrder,
      phoneNumber = "0701234567",
      savedStateHandle = savedStateHandle,
      getSwishPayinSetupStatusUseCase = statusUseCase,
      setupSwishPayinUseCase = object : SetupSwishPayinUseCase {
        override suspend fun invoke(phoneNumber: String): Either<ErrorMessage, SetupSwishResponse> =
          error("Not expected to retry")
      },
    )
}

private class FakeGetSwishPayinSetupStatusUseCase : GetSwishPayinSetupStatusUseCase {
  val responses = Channel<SwishPayinSetupStatus>(Channel.UNLIMITED)
  val polledOrderIds = mutableListOf<String>()

  override suspend fun invoke(orderId: String): Either<ErrorMessage, SwishPayinSetupStatus> {
    polledOrderIds += orderId
    return responses.receive().right()
  }
}
