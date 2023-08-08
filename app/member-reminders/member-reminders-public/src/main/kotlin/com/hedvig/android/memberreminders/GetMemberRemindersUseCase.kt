package com.hedvig.android.memberreminders

import arrow.core.NonEmptyList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

interface GetMemberRemindersUseCase {
  fun invoke(): Flow<MemberReminders>
}

internal class GetMemberRemindersUseCaseImpl(
  private val enableNotificationsReminderManager: EnableNotificationsReminderManager,
  private val getConnectPaymentReminderUseCase: GetConnectPaymentReminderUseCase,
  private val getUpcomingRenewalRemindersUseCase: GetUpcomingRenewalRemindersUseCase,
) : GetMemberRemindersUseCase {
  override fun invoke(): Flow<MemberReminders> {
    return combine(
      enableNotificationsReminderManager.showNotificationReminder().map { showReminder ->
        if (showReminder) {
          MemberReminder.EnableNotifications
        } else {
          null
        }
      },
      flow {
        emit(getConnectPaymentReminderUseCase.invoke().getOrNull()?.let { MemberReminder.ConnectPayment })
      },
      flow {
        val upcomingRenewals = getUpcomingRenewalRemindersUseCase.invoke().getOrNull()?.let {
          MemberReminder.UpcomingRenewals(it)
        }
        emit(upcomingRenewals)
      },
    ) {
        enableNotifications: MemberReminder.EnableNotifications?,
        connectPayment: MemberReminder.ConnectPayment?,
        upcomingRenewalReminders: MemberReminder.UpcomingRenewals?,
      ->
      MemberReminders(
        connectPayment = connectPayment,
        upcomingRenewals = upcomingRenewalReminders,
        enableNotifications = enableNotifications,
      )
    }
  }
}

data class MemberReminders(
  val connectPayment: MemberReminder.ConnectPayment? = null,
  val upcomingRenewals: MemberReminder.UpcomingRenewals? = null,
  val enableNotifications: MemberReminder.EnableNotifications? = null,
) {
  /**
   * In some cases a reminder may be present but may not be applicable in our current app state.
   *
   * If [alreadyHasNotificationPermission] is true, then the notification permission reminder should not be shown.
   */
  fun onlyApplicableReminders(
    alreadyHasNotificationPermission: Boolean,
  ): ApplicableMemberReminders {
    return ApplicableMemberReminders(
      connectPayment,
      upcomingRenewals,
      enableNotifications.takeIf { !alreadyHasNotificationPermission },
    )
  }
}

data class ApplicableMemberReminders(
  val connectPayment: MemberReminder.ConnectPayment? = null,
  val upcomingRenewals: MemberReminder.UpcomingRenewals? = null,
  val enableNotifications: MemberReminder.EnableNotifications? = null,
) {
  val hasAnyReminders: Boolean
    get() = connectPayment != null || upcomingRenewals != null || enableNotifications != null
}

sealed interface MemberReminder {
  object ConnectPayment : MemberReminder
  data class UpcomingRenewals(val upcomingRenewals: NonEmptyList<UpcomingRenewal>) : MemberReminder
  object EnableNotifications : MemberReminder
}
