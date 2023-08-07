package com.hedvig.android.memberreminders

import app.cash.turbine.Turbine
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow

class TestGetMemberRemindersUseCase() : GetMemberRemindersUseCase {
  val memberReminders = Turbine<List<MemberReminder>>()
  override fun invoke(): Flow<ImmutableList<MemberReminder>> {
    return memberReminders.asChannel().receiveAsFlow().map { it.toPersistentList() }
  }
}
