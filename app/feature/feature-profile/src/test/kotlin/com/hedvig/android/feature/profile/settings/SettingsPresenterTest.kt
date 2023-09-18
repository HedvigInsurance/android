package com.hedvig.android.feature.profile.settings

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.hedvig.android.core.datastore.FakeSettingsDataStore
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager2
import com.hedvig.android.language.test.FakeLanguageService
import com.hedvig.android.market.Language
import com.hedvig.android.memberreminders.test.TestEnableNotificationsReminderManager
import com.hedvig.android.molecule.test.test
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SettingsPresenterTest {
  @Test
  fun `content stays loading as long as selectedTheme, notificationReminder or allowingSelectingTheme are uninitialized`() =
    runTest {
      val settingsDataStore = FakeSettingsDataStore()
      val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
      val featureManager = FakeFeatureManager2()
      val settingsPresenter = SettingsPresenter(
        NoopNotifyBackendAboutLanguageChangeUseCase(),
        FakeLanguageService(),
        settingsDataStore,
        enableNotificationsReminderManager,
        featureManager,
      )

      settingsPresenter.test(SettingsUiState.Loading(Language.entries.first(), Language.entries)) {
        assertThat(awaitItem()).isInstanceOf<SettingsUiState.Loading>()
        settingsDataStore.setTheme(Theme.SYSTEM_DEFAULT)
        awaitUnchanged()
        enableNotificationsReminderManager.showNotification.add(false)
        awaitUnchanged()
        featureManager.featureTurbine.add(Feature.DISABLE_DARK_MODE to true)
        assertThat(awaitItem()).isInstanceOf<SettingsUiState.Loaded>()
      }
    }

  @Test
  fun `when there's a notification reminder, show it`() = runTest {
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val settingsPresenter = SettingsPresenter(
      NoopNotifyBackendAboutLanguageChangeUseCase(),
      FakeLanguageService(),
      FakeSettingsDataStore(),
      enableNotificationsReminderManager,
      FakeFeatureManager2(),
    )

    settingsPresenter.test(
      SettingsUiState.Loaded(
        Language.EN_SE,
        listOf(Language.EN_SE, Language.SV_SE),
        Theme.SYSTEM_DEFAULT,
        showNotificationReminder = false,
        false,
      ),
    ) {
      assertThat(awaitItem().showNotificationReminder).isEqualTo(false)
      enableNotificationsReminderManager.showNotification.add(true)
      assertThat(awaitItem().showNotificationReminder).isEqualTo(true)
    }
  }

  @Test
  fun `when there's no notification reminder, keep not showing it`() = runTest {
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val settingsPresenter = SettingsPresenter(
      NoopNotifyBackendAboutLanguageChangeUseCase(),
      FakeLanguageService(),
      FakeSettingsDataStore(),
      enableNotificationsReminderManager,
      FakeFeatureManager2(),
    )

    settingsPresenter.test(
      SettingsUiState.Loaded(
        Language.EN_SE,
        listOf(Language.EN_SE, Language.SV_SE),
        Theme.SYSTEM_DEFAULT,
        showNotificationReminder = false,
        false,
      ),
    ) {
      assertThat(awaitItem().showNotificationReminder).isEqualTo(false)
      enableNotificationsReminderManager.showNotification.add(false)
      expectNoEvents()
    }
  }

  @Test
  fun `snoozing the notification correctly reports that to the service`() = runTest {
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val settingsPresenter = SettingsPresenter(
      NoopNotifyBackendAboutLanguageChangeUseCase(),
      FakeLanguageService(),
      FakeSettingsDataStore(),
      enableNotificationsReminderManager,
      FakeFeatureManager2(),
    )

    settingsPresenter.test(
      SettingsUiState.Loaded(
        Language.entries.first(),
        Language.entries,
        Theme.entries.first(),
        false,
        false,
      ),
    ) {
      enableNotificationsReminderManager.snoozeNotificationReminderCalls.expectNoEvents()
      sendEvent(SettingsEvent.SnoozeNotificationPermissionReminder)
      enableNotificationsReminderManager.snoozeNotificationReminderCalls.expectMostRecentItem()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `changing the theme with an event updates the stored value`() = runTest {
    val settingsDataStore = FakeSettingsDataStore()
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val settingsPresenter = SettingsPresenter(
      NoopNotifyBackendAboutLanguageChangeUseCase(),
      FakeLanguageService(),
      settingsDataStore,
      enableNotificationsReminderManager,
      FakeFeatureManager2(),
    )

    settingsPresenter.test(
      SettingsUiState.Loaded(
        selectedLanguage = Language.entries.first(),
        languageOptions = Language.entries,
        selectedTheme = Theme.LIGHT,
        showNotificationReminder = false,
        allowSelectingTheme = false,
      ),
    ) {
      assertThat(awaitItem().selectedTheme).isEqualTo(Theme.LIGHT)
      sendEvent(SettingsEvent.ChangeTheme(Theme.DARK))
      assertThat(awaitItem().selectedTheme).isEqualTo(Theme.DARK)
      sendEvent(SettingsEvent.ChangeTheme(Theme.DARK))
    }
  }

  @Test
  fun `when the disableDarkMode feature is on, allowing selecting a theme stays off`() = runTest {
    val featureManager = FakeFeatureManager2()
    val settingsPresenter = SettingsPresenter(
      NoopNotifyBackendAboutLanguageChangeUseCase(),
      FakeLanguageService(),
      FakeSettingsDataStore(),
      TestEnableNotificationsReminderManager(),
      featureManager,
    )

    settingsPresenter.test(
      initialState = SettingsUiState.Loaded(
        selectedLanguage = Language.entries.first(),
        languageOptions = Language.entries.toList(),
        selectedTheme = Theme.SYSTEM_DEFAULT,
        showNotificationReminder = false,
        allowSelectingTheme = false,
      ),
    ) {
      assertThat(awaitItem())
        .isInstanceOf<SettingsUiState.Loaded>()
        .prop(SettingsUiState.Loaded::allowSelectingTheme)
        .isEqualTo(false)
      featureManager.featureTurbine.add(Feature.DISABLE_DARK_MODE to true)
    }
  }

  @Test
  fun `when the disableDarkMode feature is off, allowing selecting a theme is allowed`() = runTest {
    val featureManager = FakeFeatureManager2()
    val settingsPresenter = SettingsPresenter(
      NoopNotifyBackendAboutLanguageChangeUseCase(),
      FakeLanguageService(),
      FakeSettingsDataStore(),
      TestEnableNotificationsReminderManager(),
      featureManager,
    )

    settingsPresenter.test(
      initialState = SettingsUiState.Loaded(
        selectedLanguage = Language.entries.first(),
        languageOptions = Language.entries.toList(),
        selectedTheme = Theme.SYSTEM_DEFAULT,
        showNotificationReminder = false,
        allowSelectingTheme = false,
      ),
    ) {
      assertThat(awaitItem())
        .isInstanceOf<SettingsUiState.Loaded>()
        .prop(SettingsUiState.Loaded::allowSelectingTheme)
        .isEqualTo(false)
      expectNoEvents()
      featureManager.featureTurbine.add(Feature.DISABLE_DARK_MODE to false)
      assertThat(awaitItem())
        .isInstanceOf<SettingsUiState.Loaded>()
        .prop(SettingsUiState.Loaded::allowSelectingTheme)
        .isEqualTo(true)
    }
  }
}

private class NoopNotifyBackendAboutLanguageChangeUseCase : NotifyBackendAboutLanguageChangeUseCase {
  override suspend fun invoke(language: Language) {
  }
}
