package com.hedvig.android.feature.profile.tab

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.MainCoroutineRule
import com.hedvig.android.feature.profile.data.CheckCertificatesAvailabilityUseCase
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.memberreminders.test.TestEnableNotificationsReminderSnoozeManager
import com.hedvig.android.memberreminders.test.TestGetMemberRemindersUseCase
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ProfilePresenterTest {
  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  private val noopLogoutUseCase = object : LogoutUseCase {
    override fun invoke() {
      // no-op
    }
  }

  @Test
  fun `when euro bonus does not exist, should not show the EuroBonus status`() = runTest {
    val certificatesAvailabilityUseCase = FakeCheckCertificatesAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply {
        turbine.add(GetEurobonusError.EurobonusNotApplicable.left())
      },
      certificatesAvailabilityUseCase.apply { turbine.add(Unit.right()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderSnoozeManager(),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = null,
          certificatesAvailable = true,
          showPaymentScreen = true,
          showClaimHistory = true,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when euro bonus exists, should show the EuroBonus status`() = runTest {
    val certificatesAvailabilityUseCase = FakeCheckCertificatesAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply {
        turbine.add(EuroBonus("code1234").right())
      },
      certificatesAvailabilityUseCase.apply { turbine.add(Unit.right()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderSnoozeManager(),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = EuroBonus("code1234"),
          certificatesAvailable = true,
          showPaymentScreen = true,
          showClaimHistory = true,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when travel certificates are available should show travel certificate button`() = runTest {
    val certificatesAvailabilityUseCase = FakeCheckCertificatesAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply {
        turbine.add(EuroBonus("code1234").right())
      },
      certificatesAvailabilityUseCase.apply { turbine.add(Unit.right()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderSnoozeManager(),
      noopLogoutUseCase,
    )
    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = EuroBonus("code1234"),
          certificatesAvailable = true,
          showPaymentScreen = true,
          showClaimHistory = true,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when travel certificates are not available should not show travel certificate button`() = runTest {
    val certificatesAvailabilityUseCase = FakeCheckCertificatesAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply {
        turbine.add(EuroBonus("code1234").right())
      },
      certificatesAvailabilityUseCase.apply {
        turbine.add(ErrorMessage().left())
      },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderSnoozeManager(),
      noopLogoutUseCase,
    )
    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = EuroBonus("code1234"),
          certificatesAvailable = false,
          showPaymentScreen = true,
          showClaimHistory = true,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Initially all optional items are off, and as they come in, they show one by one`() = runTest {
    val euroBonusStatusUseCase = FakeGetEurobonusStatusUseCase()
    val getMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val certificatesAvailabilityUseCase = FakeCheckCertificatesAvailabilityUseCase()

    val presenter = ProfilePresenter(
      euroBonusStatusUseCase,
      certificatesAvailabilityUseCase,
      getMemberRemindersUseCase,
      TestEnableNotificationsReminderSnoozeManager(),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      euroBonusStatusUseCase.turbine.add(EuroBonus("1234").right())
      getMemberRemindersUseCase.memberReminders.add(MemberReminders())
      certificatesAvailabilityUseCase.turbine.add(Unit.right())
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = EuroBonus("1234"),
          certificatesAvailable = true,
          memberReminders = MemberReminders(
            connectPayment = null,
            upcomingRenewals = null,
            enableNotifications = null,
          ),
          showClaimHistory = true,
          showPaymentScreen = true,
        ),
      )
      getMemberRemindersUseCase.memberReminders.add(
        MemberReminders(
          connectPayment = MemberReminder.PaymentReminder.ConnectPayment(),
        ),
      )
      runCurrent()
      val connectPayment = (awaitItem() as ProfileUiState.Success).memberReminders.connectPayment
      assertThat(connectPayment).isNotNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when there are no reminders to show, uiState has an empty list of reminders`() = runTest {
    val getMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val certificatesAvailabilityUseCase = FakeCheckCertificatesAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
      certificatesAvailabilityUseCase.apply {
        turbine.add(ErrorMessage().left())
      },
      getMemberRemindersUseCase,
      TestEnableNotificationsReminderSnoozeManager(),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()

      getMemberRemindersUseCase.memberReminders.add(MemberReminders())
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = null,
          certificatesAvailable = false,
          memberReminders = MemberReminders(),
          showClaimHistory = true,
          showPaymentScreen = true,
        ),
      )

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when there are some reminders to show, uiState has those reminders`() = runTest {
    val getMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val certificatesAvailabilityUseCase = FakeCheckCertificatesAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
      certificatesAvailabilityUseCase.apply {
        turbine.add(ErrorMessage().left())
      },
      getMemberRemindersUseCase,
      TestEnableNotificationsReminderSnoozeManager(),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()

      getMemberRemindersUseCase.memberReminders.add(
        MemberReminders(
          connectPayment = MemberReminder.PaymentReminder.ConnectPayment(),
          enableNotifications = MemberReminder.EnableNotifications(),
        ),
      )
      runCurrent()
      assertAll {
        val state = awaitItem()
        val connectPayment = (state as ProfileUiState.Success).memberReminders.connectPayment
        val enableNotifications = state.memberReminders.enableNotifications
        assertThat(connectPayment).isNotNull()
        assertThat(enableNotifications).isNotNull()
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when there are errors, retrying and getting good data should reflect in the ui state`() = runTest {
    val getMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val getEurobonusStatusUseCase = FakeGetEurobonusStatusUseCase()
    val certificatesAvailabilityUseCase = FakeCheckCertificatesAvailabilityUseCase()
    val presenter = ProfilePresenter(
      getEurobonusStatusUseCase,
      certificatesAvailabilityUseCase,
      getMemberRemindersUseCase,
      TestEnableNotificationsReminderSnoozeManager(),
      noopLogoutUseCase,
    )
    val testId = "test"

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      certificatesAvailabilityUseCase.turbine.add(
        ErrorMessage().left(),
      )
      getMemberRemindersUseCase.memberReminders.add(MemberReminders())
      getEurobonusStatusUseCase.turbine.add(GetEurobonusError.Error(ErrorMessage()).left())
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = null,
          certificatesAvailable = false,
          showPaymentScreen = true,
          showClaimHistory = true,
          memberReminders = MemberReminders(),
        ),
      )
      sendEvent(ProfileUiEvent.Reload)
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      getEurobonusStatusUseCase.turbine.add(EuroBonus("abc").right())
      certificatesAvailabilityUseCase.apply { turbine.add(Unit.right()) }
      getMemberRemindersUseCase.memberReminders.add(
        MemberReminders(connectPayment = MemberReminder.PaymentReminder.ConnectPayment(id = testId)),
      )
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = EuroBonus("abc"),
          certificatesAvailable = true,
          showPaymentScreen = true,
          showClaimHistory = true,
          memberReminders = MemberReminders(
            connectPayment = MemberReminder.PaymentReminder.ConnectPayment(id = testId),
          ),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }
}

private class FakeGetEurobonusStatusUseCase() : GetEurobonusStatusUseCase {
  val turbine = Turbine<Either<GetEurobonusError, EuroBonus>>(name = "EurobonusResponse")

  override suspend fun invoke(): Either<GetEurobonusError, EuroBonus> {
    return turbine.awaitItem()
  }
}

private class FakeCheckCertificatesAvailabilityUseCase :
  CheckCertificatesAvailabilityUseCase {
  val turbine = Turbine<Either<ErrorMessage, Unit>>(
    name = "TravelCertificateAvailability response",
  )

  override suspend fun invoke(): Either<ErrorMessage, Unit> {
    return turbine.awaitItem()
  }
}
