package com.hedvig.app.feature.profile.ui.tab

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.feature.profile.data.ProfileRepository
import giraffe.ProfileQuery
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

internal class ProfileViewModel(
  private val profileRepository: ProfileRepository,
  private val logoutUseCase: LogoutUseCase,
  private val profileQueryDataToProfileUiStateMapper: ProfileQueryDataToProfileUiStateMapper,
) : ViewModel() {

  private val observeProfileRetryChannel = RetryChannel()
  val data: StateFlow<ProfileUiState> = observeProfileRetryChannel
    .flatMapLatest {
      profileRepository
        .profile()
        .mapLatest { profileQueryDataResult ->
          profileQueryDataResult.map { profileQueryDataToProfileUiStateMapper.map(it) }
        }
        .mapLatest { profileUiStateResult ->
          when (profileUiStateResult) {
            is Either.Left -> {
              ProfileUiState.Error
            }
            is Either.Right -> {
              ProfileUiState.Success(profileUiStateResult.value)
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

internal data class ProfileUiState(
  val member: Member,
  val contactInfoName: String,
  val showBusinessModel: Boolean,
  val paymentState: PaymentState,
)

internal data class Member(
  val email: String?,
  val phoneNumber: String?,
) {
  companion object {
    fun fromDto(dto: ProfileQuery.Member): Member {
      return Member(
        email = dto.email,
        phoneNumber = dto.phoneNumber,
      )
    }
  }
}

internal sealed interface PaymentState {
  data class Show(
    val monetaryMonthlyNet: String,
    @StringRes val priceCaptionResId: Int?,
  ) : PaymentState

  object DontShow : PaymentState
}
