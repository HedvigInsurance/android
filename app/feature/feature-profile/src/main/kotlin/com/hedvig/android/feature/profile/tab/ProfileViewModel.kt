package com.hedvig.android.feature.profile.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.data.travelcertificate.CheckTravelCertificateDestinationAvailabilityUseCase
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.memberreminders.EnableNotificationsReminderManager
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import com.hedvig.android.memberreminders.MemberReminders
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class ProfileViewModel(
  private val getEuroBonusStatusUseCase: GetEurobonusStatusUseCase,
  private val checkTravelCertificateDestinationAvailabilityUseCase:
    CheckTravelCertificateDestinationAvailabilityUseCase,
  private val getMemberRemindersUseCase: GetMemberRemindersUseCase,
  private val enableNotificationsReminderManager: EnableNotificationsReminderManager,
  private val featureManager: FeatureManager,
  private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
  private val retryChannel = RetryChannel()

  val data: StateFlow<ProfileUiState> = retryChannel.flatMapLatest {
    combine(
      getMemberRemindersUseCase.invoke(),
      featureManager.isFeatureEnabled(Feature.PAYMENT_SCREEN),
      flow { emit(getEuroBonusStatusUseCase.invoke()) },
      flow { emit(checkTravelCertificateDestinationAvailabilityUseCase.invoke()) },
    ) { memberReminders, isPaymentScreenFeatureEnabled, eurobonusResponse, travelCertificateAvailability ->
      ProfileUiState(
        euroBonus = eurobonusResponse.getOrNull(),
        showPaymentScreen = isPaymentScreenFeatureEnabled,
        memberReminders = memberReminders,
        isLoading = false,
        travelCertificateAvailable = travelCertificateAvailability.isRight(),
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

  fun snoozeNotificationPermission() {
    viewModelScope.launch {
      enableNotificationsReminderManager.snoozeNotificationReminder()
    }
  }

  fun onLogout() {
    logoutUseCase.invoke()
  }
}

internal data class ProfileUiState(
  val euroBonus: EuroBonus? = null,
  val travelCertificateAvailable: Boolean = true,
  val showPaymentScreen: Boolean = false,
  val memberReminders: MemberReminders = MemberReminders(),
  val isLoading: Boolean = true,
)
