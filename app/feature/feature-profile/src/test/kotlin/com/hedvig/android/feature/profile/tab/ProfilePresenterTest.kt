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
import com.hedvig.android.feature.profile.data.CheckTravelCertificateDestinationAvailabilityUseCase
import com.hedvig.android.feature.profile.data.TravelCertificateAvailabilityError
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.memberreminders.test.TestEnableNotificationsReminderManager
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
  fun `when payment-feature is not activated, should not show payment-data`() = runTest {
    val travelCertificateAvailabilityUseCase = FakeCheckTravelCertificateDestinationAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
      travelCertificateAvailabilityUseCase.apply { turbine.add(Unit.right()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderManager(),
      FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.PAYMENT_SCREEN to false,
            Feature.HELP_CENTER to true,
          )
        },
      ),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = null,
          travelCertificateAvailable = true,
          showPaymentScreen = false,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when payment-feature is activated, should show payment data`() = runTest {
    val travelCertificateAvailabilityUseCase = FakeCheckTravelCertificateDestinationAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
      travelCertificateAvailabilityUseCase.apply { turbine.add(Unit.right()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderManager(),
      FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.PAYMENT_SCREEN to true,
            Feature.HELP_CENTER to true,
          )
        },
      ),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = null,
          travelCertificateAvailable = true,
          showPaymentScreen = true,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when payment-feature is activated, but response fails, should not show payment data`() = runTest {
    val travelCertificateAvailabilityUseCase = FakeCheckTravelCertificateDestinationAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
      travelCertificateAvailabilityUseCase.apply { turbine.add(Unit.right()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderManager(),
      FakeFeatureManager(noopFeatureManager = true),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = null,
          travelCertificateAvailable = true,
          showPaymentScreen = false,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when euro bonus does not exist, should not show the EuroBonus status`() = runTest {
    val travelCertificateAvailabilityUseCase = FakeCheckTravelCertificateDestinationAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
      travelCertificateAvailabilityUseCase.apply { turbine.add(Unit.right()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderManager(),
      FakeFeatureManager(noopFeatureManager = true),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = null,
          travelCertificateAvailable = true,
          showPaymentScreen = false,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when euro bonus exists, should show the EuroBonus status`() = runTest {
    val travelCertificateAvailabilityUseCase = FakeCheckTravelCertificateDestinationAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(EuroBonus("code1234").right()) },
      travelCertificateAvailabilityUseCase.apply { turbine.add(Unit.right()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderManager(),
      FakeFeatureManager(noopFeatureManager = true),
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = EuroBonus("code1234"),
          travelCertificateAvailable = true,
          showPaymentScreen = false,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when travel certificates are available should show travel certificate button`() = runTest {
    val travelCertificateAvailabilityUseCase = FakeCheckTravelCertificateDestinationAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(EuroBonus("code1234").right()) },
      travelCertificateAvailabilityUseCase.apply { turbine.add(Unit.right()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderManager(),
      FakeFeatureManager(noopFeatureManager = true),
      noopLogoutUseCase,
    )
    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = EuroBonus("code1234"),
          travelCertificateAvailable = true,
          showPaymentScreen = false,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when travel certificates are not available should not show travel certificate button`() = runTest {
    val travelCertificateAvailabilityUseCase = FakeCheckTravelCertificateDestinationAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(EuroBonus("code1234").right()) },
      travelCertificateAvailabilityUseCase.apply {
        turbine.add(TravelCertificateAvailabilityError.TravelCertificateNotAvailable.left())
      },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderManager(),
      FakeFeatureManager(noopFeatureManager = true),
      noopLogoutUseCase,
    )
    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = EuroBonus("code1234"),
          travelCertificateAvailable = false,
          showPaymentScreen = false,
          memberReminders = MemberReminders(),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Initially all optional items are off, and as they come in, they show one by one`() = runTest {
    val featureManager = FakeFeatureManager2(
      fixedMap = mapOf(Feature.PAYMENT_SCREEN to true, Feature.HELP_CENTER to true),
    )
    val euroBonusStatusUseCase = FakeGetEurobonusStatusUseCase()
    val getMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val travelCertificateAvailabilityUseCase = FakeCheckTravelCertificateDestinationAvailabilityUseCase()

    val presenter = ProfilePresenter(
      euroBonusStatusUseCase,
      travelCertificateAvailabilityUseCase,
      getMemberRemindersUseCase,
      TestEnableNotificationsReminderManager(),
      featureManager,
      noopLogoutUseCase,
    )

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      euroBonusStatusUseCase.turbine.add(EuroBonus("1234").right())
      getMemberRemindersUseCase.memberReminders.add(MemberReminders())
      travelCertificateAvailabilityUseCase.turbine.add(Unit.right())
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = EuroBonus("1234"),
          travelCertificateAvailable = true,
          memberReminders = MemberReminders(
            connectPayment = null,
            upcomingRenewals = null,
            enableNotifications = null,
          ),
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
    val travelCertificateAvailabilityUseCase = FakeCheckTravelCertificateDestinationAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
      travelCertificateAvailabilityUseCase.apply {
        turbine.add(TravelCertificateAvailabilityError.TravelCertificateNotAvailable.left())
      },
      getMemberRemindersUseCase,
      TestEnableNotificationsReminderManager(),
      FakeFeatureManager2(mapOf(Feature.PAYMENT_SCREEN to false, Feature.HELP_CENTER to true)),
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
          travelCertificateAvailable = false,
          memberReminders = MemberReminders(),
          showPaymentScreen = false,
        ),
      )

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when there are some reminders to show, uiState has those reminders`() = runTest {
    val getMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val travelCertificateAvailabilityUseCase = FakeCheckTravelCertificateDestinationAvailabilityUseCase()
    val presenter = ProfilePresenter(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
      travelCertificateAvailabilityUseCase.apply {
        turbine.add(TravelCertificateAvailabilityError.TravelCertificateNotAvailable.left())
      },
      getMemberRemindersUseCase,
      TestEnableNotificationsReminderManager(),
      FakeFeatureManager2(mapOf(Feature.PAYMENT_SCREEN to false, Feature.HELP_CENTER to true)),
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
    val travelCertificateAvailabilityUseCase = FakeCheckTravelCertificateDestinationAvailabilityUseCase()
    val featureManager = FakeFeatureManager2()
    val presenter = ProfilePresenter(
      getEurobonusStatusUseCase,
      travelCertificateAvailabilityUseCase,
      getMemberRemindersUseCase,
      TestEnableNotificationsReminderManager(),
      featureManager,
      noopLogoutUseCase,
    )
    val testId = "test"

    presenter.test(ProfileUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      travelCertificateAvailabilityUseCase.turbine.add(
        TravelCertificateAvailabilityError.TravelCertificateNotAvailable.left(),
      )
      getMemberRemindersUseCase.memberReminders.add(MemberReminders())
      getEurobonusStatusUseCase.turbine.add(GetEurobonusError.Error(ErrorMessage()).left())
      featureManager.featureTurbine.add(Feature.PAYMENT_SCREEN to false)
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = null,
          travelCertificateAvailable = false,
          showPaymentScreen = false,
          memberReminders = MemberReminders(),
        ),
      )
      sendEvent(ProfileUiEvent.Reload)
      assertThat(awaitItem()).isInstanceOf<ProfileUiState.Loading>()
      runCurrent()
      getEurobonusStatusUseCase.turbine.add(EuroBonus("abc").right())
      travelCertificateAvailabilityUseCase.apply { turbine.add(Unit.right()) }
      featureManager.featureTurbine.add(Feature.PAYMENT_SCREEN to true)
      getMemberRemindersUseCase.memberReminders.add(
        MemberReminders(connectPayment = MemberReminder.PaymentReminder.ConnectPayment(id = testId)),
      )
      runCurrent()
      assertThat(awaitItem()).isEqualTo(
        ProfileUiState.Success(
          euroBonus = EuroBonus("abc"),
          travelCertificateAvailable = true,
          showPaymentScreen = true,
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

private class FakeCheckTravelCertificateDestinationAvailabilityUseCase :
  CheckTravelCertificateDestinationAvailabilityUseCase {
  val turbine = Turbine<Either<TravelCertificateAvailabilityError, Unit>>(
    name = "TravelCertificateAvailability response",
  )

  override suspend fun invoke(): Either<TravelCertificateAvailabilityError, Unit> {
    return turbine.awaitItem()
  }
}
