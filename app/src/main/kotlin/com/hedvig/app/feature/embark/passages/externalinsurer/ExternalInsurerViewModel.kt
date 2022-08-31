package com.hedvig.app.feature.embark.passages.externalinsurer

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExternalInsurerViewModel(
  private val getInsuranceProvidersUseCase: GetInsuranceProvidersUseCase,
  private val featureManager: FeatureManager,
) : ViewModel() {

  private val _events = Channel<Event>(Channel.UNLIMITED)
  val events = _events.receiveAsFlow()

  private val _viewState = MutableStateFlow(ViewState(isLoading = true))

  val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

  init {
    viewModelScope.launch {
      when (val result = getInsuranceProvidersUseCase.getInsuranceProviders()) {
        is InsuranceProvidersResult.Success -> _viewState.update {
          it.copy(isLoading = false, insuranceProviders = result.providers)
        }
        is InsuranceProvidersResult.Error -> {
          _viewState.update { it.copy(isLoading = false) }
          _events.trySend(Event.Error(result))
        }
      }
    }
  }

  fun selectInsuranceProvider(provider: InsuranceProvider) {
    _viewState.update { it.copy(selectedProvider = provider) }
  }

  fun continueWithProvider(provider: InsuranceProvider, resources: Resources) {
    viewModelScope.launch {
      if (provider.collectionId == null ||
        provider.collectionId == resources.getString(hedvig.resources.R.string.EXTERNAL_INSURANCE_PROVIDER_OTHER_OPTION)
      ) {
        _events.trySend(Event.CantAutomaticallyMoveInsurance)
        return@launch
      }
      if (featureManager.isFeatureEnabled(Feature.EXTERNAL_DATA_COLLECTION).not()) {
        _events.trySend(Event.SkipDataCollection)
        return@launch
      }
      _events.trySend(Event.AskForPrice(provider.collectionId, provider.name))
    }
  }

  sealed class Event {
    data class Error(val errorResult: InsuranceProvidersResult.Error) : Event()
    data class AskForPrice(val collectionId: String, val providerName: String) : Event()
    object CantAutomaticallyMoveInsurance : Event()
    object SkipDataCollection : Event()
  }

  data class ViewState(
    val isLoading: Boolean = false,
    val insuranceProviders: List<InsuranceProvider>? = null,
    val selectedProvider: InsuranceProvider? = null,
  ) {
    fun canContinue() = selectedProvider != null
  }
}
