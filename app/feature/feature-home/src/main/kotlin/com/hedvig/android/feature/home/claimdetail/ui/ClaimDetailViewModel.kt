package com.hedvig.android.feature.home.claimdetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.feature.home.claimdetail.data.GetClaimDetailUiStateFlowUseCase
import com.hedvig.android.feature.home.claimdetail.model.ClaimDetailUiState
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn

internal sealed class ClaimDetailViewState {
  data object Loading : ClaimDetailViewState()
  data object Error : ClaimDetailViewState()
  data class Content(
    val uiState: ClaimDetailUiState,
  ) : ClaimDetailViewState()
}

internal class ClaimDetailViewModel(
  private val claimId: String,
  private val getClaimDetailUiStateFlowUseCase: GetClaimDetailUiStateFlowUseCase,
  private val hAnalytics: HAnalytics,
) : ViewModel() {
  init {
    hAnalytics.screenView(AppScreen.CLAIMS_STATUS_DETAIL)
  }

  private val retryChannel = RetryChannel()
  val viewState: StateFlow<ClaimDetailViewState> = retryChannel.transformLatest {
    emit(ClaimDetailViewState.Loading)
    getClaimDetailUiStateFlowUseCase.invoke(claimId)
      .collect { result ->
        result.fold(
          ifLeft = { emit(ClaimDetailViewState.Error) },
          ifRight = { claimDetailUiState ->
            emit(ClaimDetailViewState.Content(claimDetailUiState))
          },
        )
      }
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5.seconds),
    ClaimDetailViewState.Loading,
  )

  fun retry() {
    retryChannel.retry()
  }

  fun onChatClick() {
    val uiState = (viewState.value as? ClaimDetailViewState.Content)?.uiState ?: return
    hAnalytics.claimDetailClickOpenChat(claimId, uiState.claimStatus.rawValue)
  }

  fun onPlayClick() {
    hAnalytics.claimsDetailRecordingPlayed(claimId)
  }
}
