package com.hedvig.android.memberreminders

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

interface GetMemberRemindersUseCase {
  fun invoke(): Flow<ImmutableList<MemberReminder>>
}

internal class GetMemberRemindersUseCaseImpl(
  private val enableNotificationsReminderManager: EnableNotificationsReminderManager,
  private val getConnectPaymentReminderUseCase: GetConnectPaymentReminderUseCase,
  private val getUpcomingRenewalRemindersUseCase: GetUpcomingRenewalRemindersUseCase,
) : GetMemberRemindersUseCase {
  override fun invoke(): Flow<ImmutableList<MemberReminder>> {
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
          MemberReminder.UpcomingRenewals(it.toPersistentList())
        }
        emit(upcomingRenewals)
      },
    ) {
        enableNotifications: MemberReminder.EnableNotifications?,
        connectPayment: MemberReminder.ConnectPayment?,
        upcomingRenewalReminders: MemberReminder.UpcomingRenewals?,
      ->
      listOfNotNull(
        enableNotifications,
        connectPayment,
        upcomingRenewalReminders,
      ).toPersistentList()
    }
  }
}

sealed interface MemberReminder {
  object ConnectPayment : MemberReminder
  data class UpcomingRenewals(val upcomingRenewals: ImmutableList<UpcomingRenewal>) : MemberReminder
  object EnableNotifications : MemberReminder
}
