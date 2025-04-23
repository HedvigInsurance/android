package com.hedvig.android.feature.profile.tab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.feature.profile.data.CheckTravelCertificateDestinationAvailabilityUseCase
import com.hedvig.android.feature.profile.data.TravelCertificateAvailabilityError
import com.hedvig.android.feature.profile.tab.ProfileUiEvent.Logout
import com.hedvig.android.feature.profile.tab.ProfileUiEvent.Reload
import com.hedvig.android.feature.profile.tab.ProfileUiEvent.SnoozeNotificationPermission
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.memberreminders.EnableNotificationsReminderManager
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

internal class ProfileViewModel(
  getEuroBonusStatusUseCase: GetEurobonusStatusUseCase,
  checkTravelCertificateDestinationAvailabilityUseCase: CheckTravelCertificateDestinationAvailabilityUseCase,
  getMemberRemindersUseCase: GetMemberRemindersUseCase,
  enableNotificationsReminderManager: EnableNotificationsReminderManager,
  featureManager: FeatureManager,
  logoutUseCase: LogoutUseCase,
) : MoleculeViewModel<ProfileUiEvent, ProfileUiState>(
    initialState = ProfileUiState.Loading,
    presenter = ProfilePresenter(
      getEuroBonusStatusUseCase = getEuroBonusStatusUseCase,
      checkTravelCertificateDestinationAvailabilityUseCase = checkTravelCertificateDestinationAvailabilityUseCase,
      getMemberRemindersUseCase = getMemberRemindersUseCase,
      enableNotificationsReminderManager = enableNotificationsReminderManager,
      featureManager = featureManager,
      logoutUseCase = logoutUseCase,
    ),
  )

internal class ProfilePresenter(
  private val getEuroBonusStatusUseCase: GetEurobonusStatusUseCase,
  private val checkTravelCertificateDestinationAvailabilityUseCase:
    CheckTravelCertificateDestinationAvailabilityUseCase,
  private val getMemberRemindersUseCase: GetMemberRemindersUseCase,
  private val enableNotificationsReminderManager: EnableNotificationsReminderManager,
  private val featureManager: FeatureManager,
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
        featureManager.isFeatureEnabled(Feature.PAYMENT_SCREEN),
        flow { emit(getEuroBonusStatusUseCase.invoke()) },
        flow { emit(checkTravelCertificateDestinationAvailabilityUseCase.invoke()) },
      ) { memberReminders, isPaymentScreenFeatureEnabled, eurobonusResponse, travelCertificateAvailability ->
        ProfileData(memberReminders, isPaymentScreenFeatureEnabled, eurobonusResponse, travelCertificateAvailability)
      }.collectLatest { profileData ->
        with(profileData) {
          currentState = ProfileUiState.Success(
            euroBonus = eurobonusResponse.getOrNull(),
            showPaymentScreen = isPaymentScreenFeatureEnabled,
            memberReminders = memberReminders,
            travelCertificateAvailable = travelCertificateAvailability.isRight(),
            insuranceEvidenceAvailable = false //TODO
          )
        }
      }
    }

    LaunchedEffect(snoozeNotificationReminderRequest) {
      if (snoozeNotificationReminderRequest == 0) return@LaunchedEffect
      enableNotificationsReminderManager.snoozeNotificationReminder()
    }

    return currentState
  }
}

internal sealed interface ProfileUiState {
  data class Success(
    val euroBonus: EuroBonus? = null,
    val insuranceEvidenceAvailable: Boolean,
    val travelCertificateAvailable: Boolean = true,
    val showPaymentScreen: Boolean = false,
    val memberReminders: MemberReminders = MemberReminders(),
  ) : ProfileUiState

  data object Loading : ProfileUiState
}

internal sealed interface ProfileUiEvent {
  data object Logout : ProfileUiEvent

  data object SnoozeNotificationPermission : ProfileUiEvent

  data object Reload : ProfileUiEvent
}

private data class ProfileData(
  val memberReminders: MemberReminders,
  val isPaymentScreenFeatureEnabled: Boolean,
  val eurobonusResponse: Either<GetEurobonusError, EuroBonus>,
  val travelCertificateAvailability: Either<TravelCertificateAvailabilityError, Unit>,
)
