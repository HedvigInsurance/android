package com.hedvig.android.feature.profile.settings

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.core.datastore.SettingsDataStore
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.language.test.FakeLanguageService
import com.hedvig.android.market.Language
import com.hedvig.android.memberreminders.test.TestEnableNotificationsReminderManager
import com.hedvig.android.molecule.test.test
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsPresenterTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  @Test
  fun `when there's a notification reminder, reflect that in the ui state`() = runTest {
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val settingsDataStore = SettingsDataStore(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = backgroundScope,
      ),
    )
    val settingsPresenter = SettingsPresenter(
      NoopNotifyBackendAboutLanguageChangeUseCase(),
      FakeLanguageService(),
      settingsDataStore,
      enableNotificationsReminderManager,
    )

    settingsPresenter.test(SettingsUiState.Loading(Language.EN_SE, listOf(Language.EN_SE, Language.SV_SE))) {
      assertThat(awaitItem()).isEqualTo(SettingsUiState.Loading(Language.EN_SE, listOf(Language.EN_SE, Language.SV_SE)))
      enableNotificationsReminderManager.showNotification.add(true)
      assertThat(awaitItem().showNotificationReminder).isEqualTo(true)
    }
  }

  @Test
  fun `not receiving the notification status keeps the ui state in loading mode`() = runTest {
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val settingsPresenter = SettingsPresenter(
      NoopNotifyBackendAboutLanguageChangeUseCase(),
      FakeLanguageService(),
      SettingsDataStore(
        TestPreferencesDataStore(
          datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
          coroutineScope = backgroundScope,
        ),
      ),
      enableNotificationsReminderManager,
    )

    settingsPresenter.test(SettingsUiState.Loading(Language.EN_SE, listOf(Language.EN_SE, Language.SV_SE))) {
      assertThat(awaitItem()).isEqualTo(SettingsUiState.Loading(Language.EN_SE, listOf(Language.EN_SE, Language.SV_SE)))
      sendEvent(SettingsEvent.ChangeTheme(Theme.LIGHT))
      expectNoEvents()
      enableNotificationsReminderManager.showNotification.add(false)
      assertThat(awaitItem()).isInstanceOf<SettingsUiState.Loaded>()
    }
  }

  @Test
  fun `snoozing the notification correctly reports that to the service`() = runTest {
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val settingsPresenter = SettingsPresenter(
      NoopNotifyBackendAboutLanguageChangeUseCase(),
      FakeLanguageService(),
      SettingsDataStore(
        TestPreferencesDataStore(
          datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
          coroutineScope = backgroundScope,
        ),
      ),
      enableNotificationsReminderManager,
    )

    settingsPresenter.test(SettingsUiState.Loading(Language.EN_SE, listOf(Language.EN_SE, Language.SV_SE))) {
      skipItems(1)
      enableNotificationsReminderManager.snoozeNotificationReminderCalls.expectNoEvents()
      sendEvent(SettingsEvent.SnoozeNotificationReminder)
      enableNotificationsReminderManager.snoozeNotificationReminderCalls.expectMostRecentItem()
    }
  }

  @Test
  fun `changing the theme with an event updates the stored value`() = runTest {
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val settingsDataStore = SettingsDataStore(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = backgroundScope,
      ),
    )
    val settingsPresenter = SettingsPresenter(
      NoopNotifyBackendAboutLanguageChangeUseCase(),
      FakeLanguageService(),
      settingsDataStore,
      enableNotificationsReminderManager,
    )

    settingsPresenter.test(SettingsUiState.Loading(Language.EN_SE, listOf(Language.EN_SE, Language.SV_SE))) {
      assertThat(awaitItem()).isEqualTo(SettingsUiState.Loading(Language.EN_SE, listOf(Language.EN_SE, Language.SV_SE)))
      enableNotificationsReminderManager.showNotification.add(false)
      assertThat(awaitItem().selectedTheme).isEqualTo(Theme.SYSTEM_DEFAULT)
      sendEvent(SettingsEvent.ChangeTheme(Theme.LIGHT))
      assertThat(awaitItem().selectedTheme).isEqualTo(Theme.LIGHT)
    }
  }
}

private class NoopNotifyBackendAboutLanguageChangeUseCase : NotifyBackendAboutLanguageChangeUseCase {
  override suspend fun invoke(language: Language) {
  }
}
