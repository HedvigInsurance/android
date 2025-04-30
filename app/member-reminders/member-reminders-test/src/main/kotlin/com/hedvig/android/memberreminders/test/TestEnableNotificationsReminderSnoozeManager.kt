package com.hedvig.android.memberreminders.test

import app.cash.turbine.Turbine
import com.hedvig.android.memberreminders.EnableNotificationsReminderSnoozeManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class TestEnableNotificationsReminderSnoozeManager : EnableNotificationsReminderSnoozeManager {
  val showNotification = Turbine<Boolean>()

  override fun timeToShowNotificationReminder(): Flow<Boolean> {
    return showNotification.asChannel().receiveAsFlow()
  }

  val snoozeNotificationReminderCalls = Turbine<Unit>()

  override suspend fun snoozeNotificationReminder() {
    snoozeNotificationReminderCalls.add(Unit)
  }
}
