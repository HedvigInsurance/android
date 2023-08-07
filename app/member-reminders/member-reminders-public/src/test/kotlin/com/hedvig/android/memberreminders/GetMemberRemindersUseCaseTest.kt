package com.hedvig.android.memberreminders

import app.cash.turbine.Turbine
import app.cash.turbine.test
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Test

class GetMemberRemindersUseCaseTest {

  @Test
  fun `no reminders returns an empty list`() = runTest {
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val getConnectPaymentReminderUseCase = TestGetConnectPaymentReminderUseCase()
    val getUpcomingRenewalRemindersUseCase = TestGetUpcomingRenewalRemindersUseCase()
    val getMemberRemindersUseCase = GetMemberRemindersUseCaseImpl(
      enableNotificationsReminderManager = enableNotificationsReminderManager,
      getConnectPaymentReminderUseCase = getConnectPaymentReminderUseCase,
      getUpcomingRenewalRemindersUseCase = getUpcomingRenewalRemindersUseCase,
    )

    getMemberRemindersUseCase.invoke().test {
      expectNoEvents()
      enableNotificationsReminderManager.showNotification.add(false)
      getConnectPaymentReminderUseCase.turbine.add(ConnectPaymentReminderError.AlreadySetup.left())
      getUpcomingRenewalRemindersUseCase.turbine.add(UpcomingRenewalReminderError.NoUpcomingRenewals.left())
      assertThat(awaitItem()).isEmpty()
    }
  }

  @Test
  fun `all reminders available returns a list with all of them together`() = runTest {
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val getConnectPaymentReminderUseCase = TestGetConnectPaymentReminderUseCase()
    val getUpcomingRenewalRemindersUseCase = TestGetUpcomingRenewalRemindersUseCase()
    val getMemberRemindersUseCase = GetMemberRemindersUseCaseImpl(
      enableNotificationsReminderManager = enableNotificationsReminderManager,
      getConnectPaymentReminderUseCase = getConnectPaymentReminderUseCase,
      getUpcomingRenewalRemindersUseCase = getUpcomingRenewalRemindersUseCase,
    )

    getMemberRemindersUseCase.invoke().test {
      expectNoEvents()
      enableNotificationsReminderManager.showNotification.add(true)
      getConnectPaymentReminderUseCase.turbine.add(ShowConnectPaymentReminder.right())
      getUpcomingRenewalRemindersUseCase.turbine.add(
        nonEmptyListOf(UpcomingRenewal("", LocalDate.parse("2023-01-01"), "")).right(),
      )
      assertThat(awaitItem()).containsExactly(
        MemberReminder.EnableNotifications,
        MemberReminder.ConnectPayment,
        MemberReminder.UpcomingRenewals(persistentListOf(UpcomingRenewal("", LocalDate.parse("2023-01-01"), ""))),
      )
    }
  }

  class TestEnableNotificationsReminderManager : EnableNotificationsReminderManager {
    val showNotification = Turbine<Boolean>()
    override fun showNotificationReminder(): Flow<Boolean> {
      return showNotification.asChannel().consumeAsFlow()
    }

    override suspend fun snoozeNotificationReminder() {
      error("Not needed")
    }
  }

  class TestGetConnectPaymentReminderUseCase : GetConnectPaymentReminderUseCase {
    val turbine = Turbine<Either<ConnectPaymentReminderError, ShowConnectPaymentReminder>>()
    override suspend fun invoke(): Either<ConnectPaymentReminderError, ShowConnectPaymentReminder> {
      return turbine.awaitItem()
    }
  }

  class TestGetUpcomingRenewalRemindersUseCase : GetUpcomingRenewalRemindersUseCase {
    val turbine = Turbine<Either<UpcomingRenewalReminderError, NonEmptyList<UpcomingRenewal>>>()
    override suspend fun invoke(): Either<UpcomingRenewalReminderError, NonEmptyList<UpcomingRenewal>> {
      return turbine.awaitItem()
    }
  }
}
