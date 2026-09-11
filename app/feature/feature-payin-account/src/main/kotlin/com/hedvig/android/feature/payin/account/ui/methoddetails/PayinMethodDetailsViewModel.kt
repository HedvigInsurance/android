package com.hedvig.android.feature.payin.account.ui.methoddetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.payin.account.data.GetPayinAccountUseCase
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.data.RemoveMethodUseCase
import com.hedvig.android.feature.payin.account.data.id
import com.hedvig.android.feature.payin.account.data.provider
import com.hedvig.android.feature.payin.account.navigation.PayinMethodId
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class PayinMethodDetailsViewModel(
  @Assisted methodId: PayinMethodId,
  getPayinAccountUseCase: GetPayinAccountUseCase,
  removeMethodUseCase: RemoveMethodUseCase,
) : MoleculeViewModel<PayinMethodDetailsEvent, PayinMethodDetailsUiState>(
    initialState = PayinMethodDetailsUiState.Loading,
    presenter = PayinMethodDetailsPresenter(methodId, getPayinAccountUseCase, removeMethodUseCase),
  )

internal sealed interface PayinMethodDetailsEvent {
  data object Retry : PayinMethodDetailsEvent

  data object RemoveMethod : PayinMethodDetailsEvent
}

internal sealed interface PayinMethodDetailsUiState {
  data object Loading : PayinMethodDetailsUiState

  data object Error : PayinMethodDetailsUiState

  data class Content(
    val method: PayinAccount,
    val chargingDay: Int?,
    val isRemoving: Boolean = false,
    val removeError: ErrorMessage? = null,
    val hasRemovedMethod: Boolean = false,
  ) : PayinMethodDetailsUiState
}

internal class PayinMethodDetailsPresenter(
  private val methodId: PayinMethodId,
  private val getPayinAccountUseCase: GetPayinAccountUseCase,
  private val removeMethodUseCase: RemoveMethodUseCase,
) : MoleculePresenter<PayinMethodDetailsEvent, PayinMethodDetailsUiState> {
  @Composable
  override fun MoleculePresenterScope<PayinMethodDetailsEvent>.present(
    lastState: PayinMethodDetailsUiState,
  ): PayinMethodDetailsUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var uiState by remember { mutableStateOf(lastState) }
    var methodToRemove by remember { mutableStateOf<PayinAccount?>(null) }

    LaunchedEffect(loadIteration) {
      uiState = PayinMethodDetailsUiState.Loading
      getPayinAccountUseCase.invoke().fold(
        ifLeft = { uiState = PayinMethodDetailsUiState.Error },
        ifRight = { data ->
          val method = data.currentMethods.firstOrNull { it.id == methodId }
          uiState = if (method == null) {
            PayinMethodDetailsUiState.Error
          } else {
            PayinMethodDetailsUiState.Content(method, data.chargingDay)
          }
        },
      )
    }

    LaunchedEffect(methodToRemove) {
      val method = methodToRemove ?: return@LaunchedEffect
      val content = uiState as? PayinMethodDetailsUiState.Content ?: return@LaunchedEffect
      uiState = content.copy(isRemoving = true, removeError = null)
      removeMethodUseCase.invoke(method.provider).fold(
        ifLeft = { error ->
          methodToRemove = null
          uiState = content.copy(isRemoving = false, removeError = error)
        },
        ifRight = {
          uiState = content.copy(isRemoving = true, hasRemovedMethod = true)
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        PayinMethodDetailsEvent.Retry -> {
          loadIteration++
        }

        PayinMethodDetailsEvent.RemoveMethod -> {
          if (methodToRemove == null) {
            methodToRemove = (uiState as? PayinMethodDetailsUiState.Content)?.method
          }
        }
      }
    }

    return uiState
  }
}
