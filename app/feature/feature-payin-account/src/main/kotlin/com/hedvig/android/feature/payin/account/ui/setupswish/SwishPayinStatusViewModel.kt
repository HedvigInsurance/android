package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
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
  @Assisted savedStateHandle: SavedStateHandle,
  getSwishPayinSetupStatusUseCase: GetSwishPayinSetupStatusUseCase,
  setupSwishPayinUseCase: SetupSwishPayinUseCase,
) : MoleculeViewModel<SwishPayinStatusEvent, SwishPayinStatusUiState>(
    initialState = SwishPayinStatusUiState.Loading,
    presenter = SwishPayinStatusPresenter(
      savedStateHandle = savedStateHandle,
      getSwishPayinSetupStatusUseCase = getSwishPayinSetupStatusUseCase,
      setupSwishPayinUseCase = setupSwishPayinUseCase,
    ),
  )

internal sealed interface SwishPayinStatusEvent {
  data object Retry : SwishPayinStatusEvent
}

internal sealed interface SwishPayinStatusUiState {
  data object Loading : SwishPayinStatusUiState

  data class PendingApproval(val redirectUrl: String) : SwishPayinStatusUiState

  data object Connected : SwishPayinStatusUiState

  data class Failed(val message: String?, val isRetrying: Boolean = false) : SwishPayinStatusUiState
}

@OptIn(SavedStateHandleSaveableApi::class)
internal class SwishPayinStatusPresenter(
  private val savedStateHandle: SavedStateHandle,
  private val getSwishPayinSetupStatusUseCase: GetSwishPayinSetupStatusUseCase,
  private val setupSwishPayinUseCase: SetupSwishPayinUseCase,
) : MoleculePresenter<SwishPayinStatusEvent, SwishPayinStatusUiState> {
  @Composable
  override fun MoleculePresenterScope<SwishPayinStatusEvent>.present(
    lastState: SwishPayinStatusUiState,
  ): SwishPayinStatusUiState {
    // Saved so that a member who approves in Swish while this process is killed comes back to
    // polling that same order, rather than to a fresh setup they would have to approve again.
    var order by remember(savedStateHandle) {
      savedStateHandle.saveable(key = "order", stateSaver = NullableSwishSetupOrderSaver) {
        mutableStateOf<SwishSetupOrder?>(null)
      }
    }
    var uiState by remember { mutableStateOf(lastState) }
    // Bumped by a retry. Requesting an order is skipped while one is held, so a saved order is
    // polled rather than replaced.
    var orderRequestIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(orderRequestIteration) {
      if (order != null) return@LaunchedEffect
      uiState = when (val state = uiState) {
        is SwishPayinStatusUiState.Failed -> state.copy(isRetrying = true)
        else -> SwishPayinStatusUiState.Loading
      }
      setupSwishPayinUseCase.invoke(
        "0765915968", // TODO: remove mock!!!
      ).fold(
        ifLeft = { error ->
          uiState = SwishPayinStatusUiState.Failed(error.message)
        },
        ifRight = { response ->
          val newOrder = response.order
          when {
            newOrder != null -> {
              order = newOrder
            }

            // A setup that needs no approving is already done, so there is nothing to wait on.
            response is SetupSwishResponse.Success -> {
              uiState = SwishPayinStatusUiState.Connected
            }

            else -> {
              val message = (response as? SetupSwishResponse.Failure)?.error?.message
              uiState = SwishPayinStatusUiState.Failed(message)
            }
          }
        },
      )
    }

    // Keyed on the order, so a retry's new order restarts the polling against it.
    LaunchedEffect(order) {
      val currentOrder = order ?: return@LaunchedEffect
      uiState = SwishPayinStatusUiState.PendingApproval(currentOrder.successUrl)
      while (isActive) {
        when (val status = getSwishPayinSetupStatusUseCase.invoke(currentOrder.orderId).getOrNull()) {
          SwishPayinSetupStatus.Active -> {
            uiState = SwishPayinStatusUiState.Connected
            break
          }

          is SwishPayinSetupStatus.Failed -> {
            uiState = SwishPayinStatusUiState.Failed(status.message)
            // The failed order can not be approved any more, so a retry has to ask for a new one.
            order = null
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

    CollectEvents { event ->
      when (event) {
        SwishPayinStatusEvent.Retry -> {
          val state = uiState
          if (state is SwishPayinStatusUiState.Failed && !state.isRetrying) {
            orderRequestIteration++
          }
        }
      }
    }

    return uiState
  }
}

private val NullableSwishSetupOrderSaver = listSaver<SwishSetupOrder?, String>(
  save = { order -> if (order == null) emptyList() else listOf(order.successUrl, order.orderId) },
  restore = { saved -> if (saved.isEmpty()) null else SwishSetupOrder(successUrl = saved[0], orderId = saved[1]) },
)
