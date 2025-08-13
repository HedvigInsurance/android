package com.hedvig.android.feature.profile.tab

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.MainCoroutineRule
import com.hedvig.android.feature.profile.data.CheckCertificatesAvailabilityUseCase
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.memberreminders.test.TestEnableNotificationsReminderSnoozeManager
import com.hedvig.android.memberreminders.test.TestGetMemberRemindersUseCase
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class ProfilePresenterTest {
  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  private val noopLogoutUseCase = object : LogoutUseCase {
    override fun invoke() {
      // no-op
    }
  }

  @Test
  fun `when payment-feature is not activated, should not show payment-data`() = runTest {
    val certificatesAvailabilityUseCase = FakeCheckCertificatesAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply {
        turbine.add(GetEurobonusError.EurobonusNotApplicable.left())
      },
      certificatesAvailabilityUseCase.apply { turbine.add(Unit.right()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderSnoozeManager(),
      FakeFeatureManager2(
        fixedMap = mapOf(
          Feature.PAYMENT_SCREEN to false,
          Feature.HELP_CENTER to true,
          Feature.ENABLE_CLAIM_HISTORY to false,
        ),
      ),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = null,
          certificatesAvailable = true,
          showPaymentScreen = false,
          showClaimHistory = false,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when payment-feature is activated, should show payment data`() = runTest {
    val certificatesAvailabilityUseCase = FakeCheckCertificatesAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply {
        turbine.add(GetEurobonusError.EurobonusNotApplicable.left())
      },
      certificatesAvailabilityUseCase.apply { turbine.add(Unit.right()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderSnoozeManager(),
      FakeFeatureManager2(
        fixedMap = mapOf(
          Feature.PAYMENT_SCREEN to true,
          Feature.HELP_CENTER to true,
          Feature.ENABLE_CLAIM_HISTORY to false,
        ),
      ),
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
          showClaimHistory = false,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when payment-feature is activated, but response fails, should not show payment data`() = runTest {
    val certificatesAvailabilityUseCase = FakeCheckCertificatesAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply {
        turbine.add(GetEurobonusError.EurobonusNotApplicable.left())
      },
      certificatesAvailabilityUseCase.apply { turbine.add(Unit.right()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderSnoozeManager(),
      FakeFeatureManager2(fixedReturnForAll = false),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = null,
          certificatesAvailable = true,
          showPaymentScreen = false,
          showClaimHistory = false,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `claims history feature flag hides the navigation option`(
    @TestParameter claimHistoryFlag: Boolean,
  ) = runTest {
    println("stelios flag:$claimHistoryFlag")
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
      FakeCheckCertificatesAvailabilityUseCase().apply { turbine.add(Unit.right()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderSnoozeManager(),
      FakeFeatureManager2(
        mapOf(
          Feature.PAYMENT_SCREEN to false,
          Feature.ENABLE_CLAIM_HISTORY to claimHistoryFlag,
        ),
      ),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem())
        .isInstanceOf<ProfileUiState.Success>()
        .prop(ProfileUiState.Success::showClaimHistory)
        .run {
          if (claimHistoryFlag) isTrue() else isFalse()
        }
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
      FakeFeatureManager2(fixedReturnForAll = false),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = null,
          certificatesAvailable = true,
          showPaymentScreen = false,
          showClaimHistory = false,
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
      FakeFeatureManager2(fixedReturnForAll = false),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = EuroBonus("code1234"),
          certificatesAvailable = true,
          showPaymentScreen = false,
          showClaimHistory = false,
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
      FakeFeatureManager2(fixedReturnForAll = false),
      noopLogoutUseCase,
    )
    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = EuroBonus("code1234"),
          certificatesAvailable = true,
          showPaymentScreen = false,
          showClaimHistory = false,
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
      FakeFeatureManager2(fixedReturnForAll = false),
      noopLogoutUseCase,
    )
    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = EuroBonus("code1234"),
          certificatesAvailable = false,
          showPaymentScreen = false,
          showClaimHistory = false,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Initially all optional items are off, and as they come in, they show one by one`() = runTest {
    val featureManager = FakeFeatureManager2(
      fixedMap = mapOf(
        Feature.PAYMENT_SCREEN to true,
        Feature.HELP_CENTER to true,
        Feature.ENABLE_CLAIM_HISTORY to false,
      ),
    )
    val euroBonusStatusUseCase = FakeGetEurobonusStatusUseCase()
    val getMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val certificatesAvailabilityUseCase = FakeCheckCertificatesAvailabilityUseCase()

    val presenter = ProfilePresenter(
      euroBonusStatusUseCase,
      certificatesAvailabilityUseCase,
      getMemberRemindersUseCase,
      TestEnableNotificationsReminderSnoozeManager(),
      featureManager,
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
          showClaimHistory = false,
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
      FakeFeatureManager2(
        mapOf(
          Feature.PAYMENT_SCREEN to false,
          Feature.HELP_CENTER to true,
          Feature.ENABLE_CLAIM_HISTORY to false,
        ),
      ),
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
          showClaimHistory = false,
          showPaymentScreen = false,
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
      FakeFeatureManager2(
        mapOf(
          Feature.PAYMENT_SCREEN to false,
          Feature.HELP_CENTER to true,
          Feature.ENABLE_CLAIM_HISTORY to false,
        ),
      ),
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
    val featureManager = FakeFeatureManager2(mapOf(Feature.ENABLE_CLAIM_HISTORY to false))
    val presenter = ProfilePresenter(
      getEurobonusStatusUseCase,
      certificatesAvailabilityUseCase,
      getMemberRemindersUseCase,
      TestEnableNotificationsReminderSnoozeManager(),
      featureManager,
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
      featureManager.featureTurbine.add(Feature.PAYMENT_SCREEN to false)
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = null,
          certificatesAvailable = false,
          showPaymentScreen = false,
          showClaimHistory = false,
          memberReminders = MemberReminders(),
        ),
      )
      sendEvent(ProfileUiEvent.Reload)
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      getEurobonusStatusUseCase.turbine.add(EuroBonus("abc").right())
      certificatesAvailabilityUseCase.apply { turbine.add(Unit.right()) }
      featureManager.featureTurbine.add(Feature.PAYMENT_SCREEN to true)
      getMemberRemindersUseCase.memberReminders.add(
        MemberReminders(connectPayment = MemberReminder.PaymentReminder.ConnectPayment(id = testId)),
      )
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = EuroBonus("abc"),
          certificatesAvailable = true,
          showPaymentScreen = true,
          showClaimHistory = false,
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
