package com.hedvig.android.feature.businessmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

internal class BusinessModelViewModel(
  private val getBusinessModelInformationUseCase: GetBusinessModelInformationUseCase,
  hAnalytics: HAnalytics,
) : ViewModel() {

  init {
    hAnalytics.screenView(AppScreen.CHARITY)
  }

  private val retryChannel = RetryChannel()
  private val isLoading = MutableStateFlow(false)
  private val businessModelInformation: Flow<BusinessModelInformation?> = retryChannel.transformLatest {
    val businessModelInformation = getBusinessModelInformationUseCase.invoke()
    emit(businessModelInformation)
    isLoading.update { false }
  }

  val uiState: StateFlow<BusinessModelUiState> = combine(
    isLoading,
    businessModelInformation,
  ) { isLoading, businessModelInformation ->
    BusinessModelUiState(businessModelInformation, isLoading)
  }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5.seconds),
      BusinessModelUiState(null, true),
    )

  fun reload() {
    isLoading.update { true }
    retryChannel.retry()
  }
}

internal data class BusinessModelUiState(
  val businessModelInformation: BusinessModelInformation?,
  val isLoading: Boolean,
)
