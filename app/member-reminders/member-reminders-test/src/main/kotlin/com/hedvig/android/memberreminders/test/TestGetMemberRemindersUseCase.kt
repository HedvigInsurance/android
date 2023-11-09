package com.hedvig.android.memberreminders.test

import app.cash.turbine.Turbine
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import com.hedvig.android.memberreminders.MemberReminders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class TestGetMemberRemindersUseCase() : GetMemberRemindersUseCase {
  val memberReminders = Turbine<MemberReminders>()

  override fun invoke(): Flow<MemberReminders> {
    return memberReminders.asChannel().receiveAsFlow()
  }
}
