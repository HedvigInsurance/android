package com.hedvig.app.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.feature.profile.ui.tab.ProfileQueryDataToProfileUiStateMapper
import com.hedvig.app.feature.profile.ui.tab.ProfileUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class ProfileViewModel(
  private val profileRepository: ProfileRepository,
  private val logoutUseCase: LogoutUseCase,
  private val profileQueryDataToProfileUiStateMapper: ProfileQueryDataToProfileUiStateMapper,
) : ViewModel() {
  sealed interface UiState {
    data class Success(val profileUiState: ProfileUiState) : UiState
    object Error : UiState
    object Loading : UiState
  }

  private val observeProfileRetryChannel = RetryChannel()
  val data: StateFlow<UiState> = observeProfileRetryChannel
    .flatMapLatest {
      profileRepository
        .profile()
        .mapLatest { profileQueryDataResult ->
          profileQueryDataResult.map { profileQueryDataToProfileUiStateMapper.map(it) }
        }
        .mapLatest { profileUiStateResult ->
          when (profileUiStateResult) {
            is Either.Left -> {
              UiState.Error
            }
            is Either.Right -> {
              UiState.Success(profileUiStateResult.value)
            }
          }
        }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = UiState.Loading,
    )

  fun reload() {
    observeProfileRetryChannel.retry()
  }

  fun onLogout() {
    logoutUseCase.invoke()
  }
}
