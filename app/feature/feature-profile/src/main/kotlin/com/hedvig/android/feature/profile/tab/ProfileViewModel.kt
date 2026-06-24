package com.hedvig.android.feature.profile.tab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.profile.data.CheckCertificatesAvailabilityUseCase
import com.hedvig.android.feature.profile.tab.ProfileUiEvent.Logout
import com.hedvig.android.feature.profile.tab.ProfileUiEvent.Reload
import com.hedvig.android.feature.profile.tab.ProfileUiEvent.SnoozeNotificationPermission
import com.hedvig.android.memberreminders.EnableNotificationsReminderSnoozeManager
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class ProfileViewModel(
  getEuroBonusStatusUseCase: GetEurobonusStatusUseCase,
  checkCertificatesAvailabilityUseCase: CheckCertificatesAvailabilityUseCase,
  getMemberRemindersUseCase: GetMemberRemindersUseCase,
  enableNotificationsReminderSnoozeManager: EnableNotificationsReminderSnoozeManager,
  logoutUseCase: LogoutUseCase,
) : MoleculeViewModel<ProfileUiEvent, ProfileUiState>(
    initialState = ProfileUiState.Loading,
    presenter = ProfilePresenter(
      getEuroBonusStatusUseCase = getEuroBonusStatusUseCase,
      checkCertificatesAvailabilityUseCase = checkCertificatesAvailabilityUseCase,
      getMemberRemindersUseCase = getMemberRemindersUseCase,
      enableNotificationsReminderSnoozeManager = enableNotificationsReminderSnoozeManager,
      logoutUseCase = logoutUseCase,
    ),
  )

internal class ProfilePresenter(
  private val getEuroBonusStatusUseCase: GetEurobonusStatusUseCase,
  private val checkCertificatesAvailabilityUseCase: CheckCertificatesAvailabilityUseCase,
  private val getMemberRemindersUseCase: GetMemberRemindersUseCase,
  private val enableNotificationsReminderSnoozeManager: EnableNotificationsReminderSnoozeManager,
  private val logoutUseCase: LogoutUseCase,
) : MoleculePresenter<ProfileUiEvent, ProfileUiState> {
  @Composable
  override fun MoleculePresenterScope<ProfileUiEvent>.present(lastState: ProfileUiState): ProfileUiState {
    var dataLoadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }
    var snoozeNotificationReminderRequest by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        Logout -> logoutUseCase.invoke()
        SnoozeNotificationPermission -> snoozeNotificationReminderRequest++
        Reload -> dataLoadIteration++
      }
    }

    LaunchedEffect(dataLoadIteration) {
      if (lastState !is ProfileUiState.Success) {
        currentState = ProfileUiState.Loading
      }
      combine(
        getMemberRemindersUseCase.invoke(),
        flow { emit(getEuroBonusStatusUseCase.invoke()) },
        flow { emit(checkCertificatesAvailabilityUseCase.invoke()) },
      ) {
        memberReminders,
        eurobonusResponse,
        certificatesAvailability,
        ->
        ProfileUiState.Success(
          euroBonus = eurobonusResponse.getOrNull(),
          showPaymentScreen = true,
          memberReminders = memberReminders,
          showClaimHistory = true,
          certificatesAvailable = certificatesAvailability.isRight(),
        )
      }.collectLatest { state ->
        currentState = state
      }
    }

    LaunchedEffect(snoozeNotificationReminderRequest) {
      if (snoozeNotificationReminderRequest == 0) return@LaunchedEffect
      enableNotificationsReminderSnoozeManager.snoozeNotificationReminder()
    }

    return currentState
  }
}

internal sealed interface ProfileUiState {
  data class Success(
    val certificatesAvailable: Boolean,
    val euroBonus: EuroBonus? = null,
    val showPaymentScreen: Boolean = false,
    val showClaimHistory: Boolean,
    val memberReminders: MemberReminders = MemberReminders(),
  ) : ProfileUiState

  data object Loading : ProfileUiState
}

internal sealed interface ProfileUiEvent {
  data object Logout : ProfileUiEvent

  data object SnoozeNotificationPermission : ProfileUiEvent

  data object Reload : ProfileUiEvent
}
