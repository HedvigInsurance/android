package com.hedvig.android.feature.profile.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import com.hedvig.android.memberreminders.MemberReminder
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

internal class ProfileViewModel(
  private val getEuroBonusStatusUseCase: GetEurobonusStatusUseCase,
  private val getMemberRemindersUseCase: GetMemberRemindersUseCase,
  private val featureManager: FeatureManager,
  private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

  private val retryChannel = RetryChannel()

  val data: StateFlow<ProfileUiState> = retryChannel.flatMapLatest {
    combine(
      getMemberRemindersUseCase.invoke(),
      flow { emit(featureManager.isFeatureEnabled(Feature.PAYMENT_SCREEN)) },
      flow { emit(getEuroBonusStatusUseCase.invoke()) },
    ) { memberReminders, isPaymentScreenFeatureEnabled, eurobonusResponse ->
      ProfileUiState(
        euroBonus = eurobonusResponse.getOrNull(),
        showPaymentScreen = isPaymentScreenFeatureEnabled,
        memberReminders = memberReminders,
        isLoading = false,
      )
    }
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5.seconds),
    ProfileUiState(),
  )

  fun reload() {
    retryChannel.retry()
  }

  fun onLogout() {
    logoutUseCase.invoke()
  }
}

internal data class ProfileUiState(
  val euroBonus: EuroBonus? = null,
  val showPaymentScreen: Boolean = false,
  val memberReminders: ImmutableList<MemberReminder> = persistentListOf(),
  val isLoading: Boolean = true,
)
