package com.hedvig.android.memberreminders

import app.cash.turbine.Turbine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class TestGetMemberRemindersUseCase() : GetMemberRemindersUseCase {
  val memberReminders = Turbine<MemberReminders>()
  override fun invoke(): Flow<MemberReminders> {
    return memberReminders.asChannel().receiveAsFlow()
  }
}
