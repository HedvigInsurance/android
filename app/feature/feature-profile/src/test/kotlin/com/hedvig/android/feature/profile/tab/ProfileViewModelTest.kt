package com.hedvig.android.feature.profile.tab

import app.cash.turbine.Turbine
import app.cash.turbine.test
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.MainCoroutineRule
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.memberreminders.test.TestEnableNotificationsReminderManager
import com.hedvig.android.memberreminders.test.TestGetMemberRemindersUseCase
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {
  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  private val noopLogoutUseCase = object : LogoutUseCase {
    override fun invoke() {
      // no-op
    }
  }

  @Test
  fun `when payment-feature is not activated, should not show payment-data`() = runTest {
    val viewModel = ProfileViewModel(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
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

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.showPaymentScreen).isEqualTo(false)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when payment-feature is activated, should show payment data`() = runTest {
    val viewModel = ProfileViewModel(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
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

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.showPaymentScreen).isEqualTo(true)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when payment-feature is activated, but response fails, should not show payment data`() = runTest {
    val viewModel = ProfileViewModel(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderManager(),
      FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.PAYMENT_SCREEN to true,
            Feature.HELP_CENTER to true
          )
        },
      ),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.showPaymentScreen).isEqualTo(true)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when euro bonus does not exist, should not show the EuroBonus status`() = runTest {
    val viewModel = ProfileViewModel(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderManager(),
      FakeFeatureManager(noopFeatureManager = true),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.euroBonus).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when euro bonus exists, should show the EuroBonus status`() = runTest {
    val viewModel = ProfileViewModel(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(EuroBonus("code1234").right()) },
      TestGetMemberRemindersUseCase().apply { memberReminders.add(MemberReminders()) },
      TestEnableNotificationsReminderManager(),
      FakeFeatureManager(noopFeatureManager = true),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.euroBonus).isEqualTo(EuroBonus("code1234"))
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

    val viewModel = ProfileViewModel(
      euroBonusStatusUseCase,
      getMemberRemindersUseCase,
      TestEnableNotificationsReminderManager(),
      featureManager,
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()

      assertThat(viewModel.data.value.euroBonus).isNull()
      euroBonusStatusUseCase.turbine.add(EuroBonus("1234").right())
      getMemberRemindersUseCase.memberReminders.add(MemberReminders())
      runCurrent()
      assertThat(viewModel.data.value.euroBonus).isEqualTo(EuroBonus("1234"))
      assertThat(viewModel.data.value.showPaymentScreen).isEqualTo(true)
      assertThat(viewModel.data.value.memberReminders.connectPayment).isNull()
      assertThat(viewModel.data.value.memberReminders.upcomingRenewals).isNull()
      assertThat(viewModel.data.value.memberReminders.enableNotifications).isNull()

      getMemberRemindersUseCase.memberReminders.add(MemberReminders(connectPayment = MemberReminder.ConnectPayment()))
      runCurrent()
      assertThat(viewModel.data.value.memberReminders.connectPayment).isNotNull()
      assertThat(viewModel.data.value.memberReminders.upcomingRenewals).isNull()
      assertThat(viewModel.data.value.memberReminders.enableNotifications).isNull()

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when there are no reminders to show, uiState has an empty list of reminders`() = runTest {
    val getMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val viewModel = ProfileViewModel(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
      getMemberRemindersUseCase,
      TestEnableNotificationsReminderManager(),
      FakeFeatureManager2(mapOf(Feature.PAYMENT_SCREEN to false, Feature.HELP_CENTER to true)),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()

      getMemberRemindersUseCase.memberReminders.add(MemberReminders())
      runCurrent()
      assertThat(viewModel.data.value.memberReminders.connectPayment).isNull()
      assertThat(viewModel.data.value.memberReminders.upcomingRenewals).isNull()
      assertThat(viewModel.data.value.memberReminders.enableNotifications).isNull()

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when there are some reminders to show, uiState has those reminders`() = runTest {
    val getMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val viewModel = ProfileViewModel(
      FakeGetEurobonusStatusUseCase().apply { turbine.add(GetEurobonusError.EurobonusNotApplicable.left()) },
      getMemberRemindersUseCase,
      TestEnableNotificationsReminderManager(),
      FakeFeatureManager2(mapOf(Feature.PAYMENT_SCREEN to false, Feature.HELP_CENTER to true)),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()

      getMemberRemindersUseCase.memberReminders.add(
        MemberReminders(
          connectPayment = MemberReminder.ConnectPayment(),
          enableNotifications = MemberReminder.EnableNotifications(),
        ),
      )
      runCurrent()
      assertThat(viewModel.data.value.memberReminders.connectPayment).isNotNull()
      assertThat(viewModel.data.value.memberReminders.upcomingRenewals).isNull()
      assertThat(viewModel.data.value.memberReminders.enableNotifications).isNotNull()

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when there are errors, retrying and getting good data should reflect in the ui state`() = runTest {
    val getMemberRemindersUseCase = TestGetMemberRemindersUseCase()
    val getEurobonusStatusUseCase = FakeGetEurobonusStatusUseCase()
    val featureManager = FakeFeatureManager2()
    val viewModel = ProfileViewModel(
      getEurobonusStatusUseCase,
      getMemberRemindersUseCase,
      TestEnableNotificationsReminderManager(),
      featureManager,
      noopLogoutUseCase,
    )
    val testId = "test"

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()

      getMemberRemindersUseCase.memberReminders.add(MemberReminders())
      getEurobonusStatusUseCase.turbine.add(GetEurobonusError.Error(ErrorMessage()).left())
      featureManager.featureTurbine.add(Feature.PAYMENT_SCREEN to false)
      featureManager.featureTurbine.add(Feature.HELP_CENTER to false)
      runCurrent()
      assertThat(viewModel.data.value).isEqualTo(
        ProfileUiState(
          euroBonus = null,
          showPaymentScreen = false,
          memberReminders = MemberReminders(),
          isLoading = false,
        ),
      )

      viewModel.reload()
      runCurrent()
      getEurobonusStatusUseCase.turbine.add(EuroBonus("abc").right())
      featureManager.featureTurbine.add(Feature.PAYMENT_SCREEN to true)
      featureManager.featureTurbine.add(Feature.HELP_CENTER to true)
      getMemberRemindersUseCase.memberReminders.add(
        MemberReminders(connectPayment = MemberReminder.ConnectPayment(id = testId)),
      )
      runCurrent()
      assertThat(viewModel.data.value).isEqualTo(
        ProfileUiState(
          euroBonus = EuroBonus("abc"),
          showPaymentScreen = true,
          showHelpCenter = true,
          memberReminders = MemberReminders(connectPayment = MemberReminder.ConnectPayment(id = testId)),
          isLoading = false,
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
