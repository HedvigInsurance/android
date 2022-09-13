package com.hedvig.app.feature.referrals.ui.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.apollo.graphql.ReferralsQuery
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.app.feature.referrals.data.ReferralsRepository
import com.hedvig.android.apollo.OperationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

abstract class ReferralsViewModel : ViewModel() {
  abstract val data: StateFlow<ReferralsUiState>
  abstract fun reload()
}

class ReferralsViewModelImpl(
  private val referralsRepository: ReferralsRepository,
) : ReferralsViewModel() {
  private val retryChannel = RetryChannel()

  private val isLoading = MutableStateFlow(false)

  private val referralsResult = retryChannel.flatMapLatest {
    referralsRepository.watchReferralsQueryData().onEach {
      isLoading.update { false }
    }
  }

  override val data: StateFlow<ReferralsUiState> = combine(
    isLoading,
    referralsResult,
  ) { isLoading, referralsResult ->
    val referralsData: ReferralsQuery.Data? = (referralsResult as? OperationResult.Success)?.data
    if (referralsData == null) {
      ReferralsUiState.Error(isLoading)
    } else {
      ReferralsUiState.Success(referralsData, isLoading)
    }
  }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5.seconds),
      ReferralsUiState.Loading,
    )

  override fun reload() {
    isLoading.update { true }
    retryChannel.retry()
  }
}

sealed interface ReferralsUiState {
  val isLoading: Boolean

  data class Success(val data: ReferralsQuery.Data, override val isLoading: Boolean) : ReferralsUiState
  data class Error(override val isLoading: Boolean) : ReferralsUiState
  object Loading : ReferralsUiState {
    override val isLoading: Boolean = true
  }
}
