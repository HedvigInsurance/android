package com.hedvig.android.feature.payin.account.ui.overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.payin.account.data.GetPayinAccountUseCase
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.data.SetAsDefaultUseCase
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import octopus.type.MemberPaymentProvider

internal class PayinAccountOverviewViewModel(
  getPayinAccountUseCase: GetPayinAccountUseCase,
  setAsDefaultUseCase: SetAsDefaultUseCase
) : MoleculeViewModel<PayinAccountOverviewEvent, PayinAccountOverviewUiState>(
  PayinAccountOverviewUiState.Loading,
  PayinAccountOverviewPresenter(getPayinAccountUseCase, setAsDefaultUseCase),
)

internal sealed interface PayinAccountOverviewEvent {
  data object Retry : PayinAccountOverviewEvent

  data class SetDefaultMethod(val provider: MemberPaymentProvider): PayinAccountOverviewEvent
}

internal sealed interface PayinAccountOverviewUiState {
  data object Loading : PayinAccountOverviewUiState

  data object Error : PayinAccountOverviewUiState

  data class Content(
    val currentMethods: List<PayinAccount>,
    val availablePayinMethods: List<MemberPaymentProvider>,
    val loadingDefaultProvider: MemberPaymentProvider? =null,
    val setDefaultProviderError: ErrorMessage? = null
  ) : PayinAccountOverviewUiState
}

internal class PayinAccountOverviewPresenter(
  private val getPayinAccountUseCase: GetPayinAccountUseCase,
  private val setAsDefaultUseCase: SetAsDefaultUseCase
) : MoleculePresenter<PayinAccountOverviewEvent, PayinAccountOverviewUiState> {
  @Composable
  override fun MoleculePresenterScope<PayinAccountOverviewEvent>.present(
    lastState: PayinAccountOverviewUiState,
  ): PayinAccountOverviewUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var providerToSetAsDefault by remember { mutableStateOf<MemberPaymentProvider?>(null) }
    var uiState by remember { mutableStateOf<PayinAccountOverviewUiState>(lastState) }

    LaunchedEffect(loadIteration) {
      uiState = PayinAccountOverviewUiState.Loading
      getPayinAccountUseCase.invoke().fold(
        ifLeft = { uiState = PayinAccountOverviewUiState.Error },
        ifRight = { data ->
          uiState = PayinAccountOverviewUiState.Content(
              currentMethods = data.currentMethods,
              availablePayinMethods = data.availablePayinMethods,
            )

        },
      )
    }

    LaunchedEffect(providerToSetAsDefault) {
      val provider = providerToSetAsDefault
      if (provider!=null) {
        logcat { "Mariia: Starting LaunchedEffect(providerToSetAsDefault) with provider: $provider" }
        val currentState = uiState as?  PayinAccountOverviewUiState.Content ?: return@LaunchedEffect
        uiState = currentState.copy(loadingDefaultProvider = provider)
        setAsDefaultUseCase.invoke(provider).fold(
          ifLeft = {
            providerToSetAsDefault = null
            uiState = currentState.copy(setDefaultProviderError = it)
                   },
          ifRight = { data ->
            providerToSetAsDefault = null
            uiState = PayinAccountOverviewUiState.Content(
              currentMethods = data.currentMethods,
              availablePayinMethods = data.availablePayinMethods,
            )
          },
        )
      }
    }

    CollectEvents { event ->
      when (event) {
        PayinAccountOverviewEvent.Retry -> loadIteration++
        is PayinAccountOverviewEvent.SetDefaultMethod -> {
          val currentState = uiState as?  PayinAccountOverviewUiState.Content ?: return@CollectEvents
          uiState = currentState.copy(setDefaultProviderError = null)
          providerToSetAsDefault = event.provider
        }
      }
    }

    return uiState
  }
}
