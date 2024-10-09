package com.hedvig.android.memberreminders

import app.cash.turbine.Turbine
import app.cash.turbine.test
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isNullOrEmpty
import com.hedvig.android.memberreminders.MemberReminder.CoInsuredInfo
import com.hedvig.android.memberreminders.MemberReminder.UpcomingRenewal
import com.hedvig.android.memberreminders.test.TestEnableNotificationsReminderManager
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Test

class GetMemberRemindersUseCaseTest {
  @Test
  fun `no reminders returns all null reminders`() = runTest {
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val getConnectPaymentReminderUseCase = TestGetConnectPaymentReminderUseCase()
    val getUpcomingRenewalRemindersUseCase = TestGetUpcomingRenewalRemindersUseCase()
    val getNeedsCoInsuredInfoRemindersUseCase = TestGetNeedsCoInsuredInfoRemindersUseCase()
    val getMemberRemindersUseCase = GetMemberRemindersUseCaseImpl(
      enableNotificationsReminderManager = enableNotificationsReminderManager,
      getConnectPaymentReminderUseCase = getConnectPaymentReminderUseCase,
      getUpcomingRenewalRemindersUseCase = getUpcomingRenewalRemindersUseCase,
      getNeedsCoInsuredInfoRemindersUseCase = getNeedsCoInsuredInfoRemindersUseCase,
    )

    getMemberRemindersUseCase.invoke().test {
      expectNoEvents()
      enableNotificationsReminderManager.showNotification.add(false)
      getConnectPaymentReminderUseCase.turbine.add(ConnectPaymentReminderError.DomainError.AlreadySetup.left())
      getUpcomingRenewalRemindersUseCase.turbine.add(UpcomingRenewalReminderError.NoUpcomingRenewals.left())
      getNeedsCoInsuredInfoRemindersUseCase.turbine.add(CoInsuredInfoReminderError.NoCoInsuredReminders.left())
      assertAll {
        with(awaitItem()) {
          assertThat(this.connectPayment).isNull()
          assertThat(this.enableNotifications).isNull()
          assertThat(this.upcomingRenewals).isNull()
          assertThat(this.coInsuredInfo).isNullOrEmpty()
        }
      }
    }
  }

  @Test
  fun `all reminders available returns a list with all of them together`() = runTest {
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val getConnectPaymentReminderUseCase = TestGetConnectPaymentReminderUseCase()
    val getUpcomingRenewalRemindersUseCase = TestGetUpcomingRenewalRemindersUseCase()
    val getNeedsCoInsuredInfoRemindersUseCase = TestGetNeedsCoInsuredInfoRemindersUseCase()
    val getMemberRemindersUseCase = GetMemberRemindersUseCaseImpl(
      enableNotificationsReminderManager = enableNotificationsReminderManager,
      getConnectPaymentReminderUseCase = getConnectPaymentReminderUseCase,
      getUpcomingRenewalRemindersUseCase = getUpcomingRenewalRemindersUseCase,
      getNeedsCoInsuredInfoRemindersUseCase = getNeedsCoInsuredInfoRemindersUseCase,
    )
    val testId = "test"

    getMemberRemindersUseCase.invoke().test {
      expectNoEvents()
      enableNotificationsReminderManager.showNotification.add(true)
      getConnectPaymentReminderUseCase.turbine.add(PaymentReminder.ShowConnectPaymentReminder.right())
      getUpcomingRenewalRemindersUseCase.turbine.add(
        nonEmptyListOf(UpcomingRenewal("", LocalDate.parse("2023-01-01"), "", testId)).right(),
      )
      getNeedsCoInsuredInfoRemindersUseCase.turbine.add(
        nonEmptyListOf(CoInsuredInfo("123", testId)).right(),
      )
      assertAll {
        with(awaitItem()) {
          assertThat(this.connectPayment).isNotNull()
          assertThat(this.enableNotifications).isNotNull()
          assertThat(this.coInsuredInfo).isNotNull()
          assertThat(this.upcomingRenewals)
            .isNotNull()
            .containsExactly(
              UpcomingRenewal("", LocalDate.parse("2023-01-01"), "", testId),
            )
        }
      }
    }
  }

  class TestGetConnectPaymentReminderUseCase : GetConnectPaymentReminderUseCase {
    val turbine = Turbine<Either<ConnectPaymentReminderError, PaymentReminder.ShowConnectPaymentReminder>>()

    override suspend fun invoke(): Either<ConnectPaymentReminderError, PaymentReminder.ShowConnectPaymentReminder> {
      return turbine.awaitItem()
    }
  }

  class TestGetUpcomingRenewalRemindersUseCase : GetUpcomingRenewalRemindersUseCase {
    val turbine = Turbine<Either<UpcomingRenewalReminderError, NonEmptyList<UpcomingRenewal>>>()

    override suspend fun invoke(): Either<UpcomingRenewalReminderError, NonEmptyList<UpcomingRenewal>> {
      return turbine.awaitItem()
    }
  }

  class TestGetNeedsCoInsuredInfoRemindersUseCase : GetNeedsCoInsuredInfoRemindersUseCase {
    val turbine = Turbine<Either<CoInsuredInfoReminderError, NonEmptyList<CoInsuredInfo>>>()

    override suspend fun invoke(): Either<CoInsuredInfoReminderError, NonEmptyList<CoInsuredInfo>> {
      return turbine.awaitItem()
    }
  }
}
