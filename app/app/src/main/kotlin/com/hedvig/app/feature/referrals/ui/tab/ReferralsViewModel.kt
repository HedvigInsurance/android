package com.hedvig.app.feature.referrals.ui.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.app.feature.referrals.data.ReferralsRepository
import giraffe.ReferralTermsQuery
import giraffe.ReferralsQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

class ReferralsViewModel(
  private val referralsRepository: ReferralsRepository,
  private val getReferralTermsUseCase: GetReferralsInformationUseCase,
) : ViewModel() {
  private val retryChannel = RetryChannel()

  private val isLoading = MutableStateFlow(false)

  private val referralsResult: Flow<OperationResult<ReferralsQuery.Data>> = retryChannel.flatMapLatest {
    referralsRepository.watchReferralsQueryData().onEach {
      isLoading.update { false }
    }
  }

  val data: StateFlow<ReferralsUiState> = combine(
    isLoading,
    referralsResult,
    flow { emit(getReferralTermsUseCase.invoke()) },
  ) { isLoading, referralsResult, referralTerms ->
    val referralsData: ReferralsQuery.Data? = (referralsResult as? OperationResult.Success)?.data
    if (referralsData == null) {
      ReferralsUiState.Error(isLoading)
    } else {
      ReferralsUiState.Success(referralsData, referralTerms.getOrNull(), isLoading)
    }
  }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5.seconds),
      ReferralsUiState.Loading,
    )

  fun reload() {
    isLoading.update { true }
    retryChannel.retry()
  }
}

sealed interface ReferralsUiState {
  val isLoading: Boolean

  data class Success(
    val data: ReferralsQuery.Data,
    val referralTerms: ReferralTermsQuery.ReferralTerms?,
    override val isLoading: Boolean,
  ) : ReferralsUiState

  data class Error(override val isLoading: Boolean) : ReferralsUiState
  object Loading : ReferralsUiState {
    override val isLoading: Boolean = true
  }
}
