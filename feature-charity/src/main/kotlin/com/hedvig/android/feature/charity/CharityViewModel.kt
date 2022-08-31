package com.hedvig.android.feature.charity

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

internal class CharityViewModel(
  private val getCharityInformationUseCase: GetCharityInformationUseCase,
  hAnalytics: HAnalytics,
) : ViewModel() {

  init {
    hAnalytics.screenView(AppScreen.CHARITY)
  }

  private val retryChannel = RetryChannel()
  private val isLoading = MutableStateFlow(false)
  private val charityInformation: Flow<CharityInformation?> = retryChannel.transformLatest {
    val charityInformation = getCharityInformationUseCase.invoke()
    emit(charityInformation)
    isLoading.update { false }
  }

  val uiState: StateFlow<CharityUiState> = combine(
    isLoading,
    charityInformation,
  ) { isLoading, charityInformation ->
    CharityUiState(charityInformation, isLoading)
  }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5.seconds),
      CharityUiState(null, true),
    )

  fun reload() {
    isLoading.update { true }
    retryChannel.retry()
  }
}

internal data class CharityUiState(
  val charityInformation: CharityInformation?,
  val isLoading: Boolean,
)
