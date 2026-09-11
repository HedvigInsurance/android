package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.payin.account.data.GetSwishPayinSetupStatusUseCase
import com.hedvig.android.feature.payin.account.data.SetupSwishPayinUseCase
import com.hedvig.android.feature.payin.account.data.SetupSwishResponse
import com.hedvig.android.feature.payin.account.data.SwishPayinSetupStatus
import com.hedvig.android.feature.payin.account.data.SwishSetupOrder
import com.hedvig.android.feature.payin.account.data.order
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private val PollInterval = 2.seconds

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SwishPayinStatusViewModel(
  @Assisted successUrl: String,
  @Assisted orderId: String,
  @Assisted phoneNumber: String,
  getSwishPayinSetupStatusUseCase: GetSwishPayinSetupStatusUseCase,
  setupSwishPayinUseCase: SetupSwishPayinUseCase,
) : MoleculeViewModel<SwishPayinStatusEvent, SwishPayinStatusUiState>(
    initialState = SwishPayinStatusUiState.PendingApproval(successUrl),
    presenter = SwishPayinStatusPresenter(
      initialOrder = SwishSetupOrder(successUrl, orderId),
      phoneNumber = phoneNumber,
      getSwishPayinSetupStatusUseCase = getSwishPayinSetupStatusUseCase,
      setupSwishPayinUseCase = setupSwishPayinUseCase,
    ),
  )

internal sealed interface SwishPayinStatusEvent {
  data object Retry : SwishPayinStatusEvent
}

internal sealed interface SwishPayinStatusUiState {
  data class PendingApproval(val redirectUrl: String) : SwishPayinStatusUiState

  data object Connected : SwishPayinStatusUiState

  data class Failed(val message: String?, val isRetrying: Boolean = false) : SwishPayinStatusUiState
}

internal class SwishPayinStatusPresenter(
  private val initialOrder: SwishSetupOrder,
  private val phoneNumber: String,
  private val getSwishPayinSetupStatusUseCase: GetSwishPayinSetupStatusUseCase,
  private val setupSwishPayinUseCase: SetupSwishPayinUseCase,
) : MoleculePresenter<SwishPayinStatusEvent, SwishPayinStatusUiState> {
  @Composable
  override fun MoleculePresenterScope<SwishPayinStatusEvent>.present(
    lastState: SwishPayinStatusUiState,
  ): SwishPayinStatusUiState {
    var order by remember { mutableStateOf(initialOrder) }
    var uiState by remember { mutableStateOf(lastState) }
    var retryOnFailIteration by remember { mutableIntStateOf(0) }

    // Keyed on the order, so a retry's new order restarts the polling against it.
    LaunchedEffect(order) {
      uiState = SwishPayinStatusUiState.PendingApproval(order.successUrl)
      while (isActive) {
        when (val status = getSwishPayinSetupStatusUseCase.invoke(order.orderId).getOrNull()) {
          SwishPayinSetupStatus.Active -> {
            uiState = SwishPayinStatusUiState.Connected
            break
          }

          is SwishPayinSetupStatus.Failed -> {
            uiState = SwishPayinStatusUiState.Failed(status.message)
            break
          }

          // Still pending, or a transient failure to reach the backend; either way keep waiting,
          // since the member can leave the screen themselves.
          else -> {
            delay(PollInterval)
          }
        }
      }
    }

    LaunchedEffect(retryOnFailIteration) {
      if (retryOnFailIteration == 0) return@LaunchedEffect
      uiState = SwishPayinStatusUiState.Failed(currentFailureMessage(uiState), isRetrying = true)
      setupSwishPayinUseCase.invoke(phoneNumber).fold(
        ifLeft = { error ->
          uiState = SwishPayinStatusUiState.Failed(error.message)
        },
        ifRight = { response ->
          val newOrder = response.order
          if (newOrder != null) {
            order = newOrder
          } else {
            val message = (response as? SetupSwishResponse.Failure)?.error?.message
            uiState = SwishPayinStatusUiState.Failed(message)
          }
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        SwishPayinStatusEvent.Retry -> {
          val state = uiState
          if (state is SwishPayinStatusUiState.Failed && !state.isRetrying) {
            retryOnFailIteration++
          }
        }
      }
    }

    return uiState
  }
}

private fun currentFailureMessage(state: SwishPayinStatusUiState): String? =
  (state as? SwishPayinStatusUiState.Failed)?.message
