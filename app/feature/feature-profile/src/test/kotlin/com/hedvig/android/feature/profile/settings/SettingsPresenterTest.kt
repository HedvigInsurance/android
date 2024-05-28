package com.hedvig.android.feature.profile.settings

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.auth.listeners.UploadLanguagePreferenceToBackendUseCase
import com.hedvig.android.core.datastore.FakeSettingsDataStore
import com.hedvig.android.feature.profile.data.ChangeEmailSubscriptionPreferencesUseCase
import com.hedvig.android.feature.profile.data.SubPrefError
import com.hedvig.android.feature.profile.data.SubPrefSuccess
import com.hedvig.android.language.Language
import com.hedvig.android.language.test.FakeLanguageService
import com.hedvig.android.memberreminders.test.TestEnableNotificationsReminderManager
import com.hedvig.android.molecule.test.test
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SettingsPresenterTest {
  @Test
  fun `content stays loading as long as notificationReminder are uninitialized`() = runTest {
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val settingsPresenter = SettingsPresenter(
      FakeLanguageService(),
      FakeSettingsDataStore(),
      enableNotificationsReminderManager,
      NoopNetworkCacheManager(),
      uploadLanguagePreferenceToBackendUseCase = NoopUploadLanguagePreferenceToBackendUseCase(),
      changeEmailSubscriptionPreferencesUseCase = NoopChangeEmailSubscriptionPreferencesUseCase(),
    )

    settingsPresenter.test(SettingsUiState.Loading(Language.entries.first(), Language.entries)) {
      assertThat(awaitItem()).isInstanceOf<SettingsUiState.Loading>()
      enableNotificationsReminderManager.showNotification.add(false)
      assertThat(awaitItem()).isInstanceOf<SettingsUiState.Loaded>()
    }
  }

  @Test
  fun `when there's a notification reminder, show it`() = runTest {
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val settingsPresenter = SettingsPresenter(
      FakeLanguageService(),
      FakeSettingsDataStore(),
      enableNotificationsReminderManager,
      NoopNetworkCacheManager(),
      uploadLanguagePreferenceToBackendUseCase = NoopUploadLanguagePreferenceToBackendUseCase(),
      changeEmailSubscriptionPreferencesUseCase = NoopChangeEmailSubscriptionPreferencesUseCase(),
    )

    settingsPresenter.test(
      SettingsUiState.Loaded(
        Language.EN_SE,
        listOf(Language.EN_SE, Language.SV_SE),
        Theme.SYSTEM_DEFAULT,
        false,
        subscribed = false,
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
      FakeLanguageService(),
      FakeSettingsDataStore(),
      enableNotificationsReminderManager,
      NoopNetworkCacheManager(),
      uploadLanguagePreferenceToBackendUseCase = NoopUploadLanguagePreferenceToBackendUseCase(),
      changeEmailSubscriptionPreferencesUseCase = NoopChangeEmailSubscriptionPreferencesUseCase(),
    )

    settingsPresenter.test(
      SettingsUiState.Loaded(
        Language.EN_SE,
        listOf(Language.EN_SE, Language.SV_SE),
        Theme.SYSTEM_DEFAULT,
        false,
        subscribed = false,
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
      FakeLanguageService(),
      FakeSettingsDataStore(),
      enableNotificationsReminderManager,
      NoopNetworkCacheManager(),
      uploadLanguagePreferenceToBackendUseCase = NoopUploadLanguagePreferenceToBackendUseCase(),
      changeEmailSubscriptionPreferencesUseCase = NoopChangeEmailSubscriptionPreferencesUseCase(),
    )

    settingsPresenter.test(
      SettingsUiState.Loaded(
        Language.entries.first(),
        Language.entries,
        Theme.entries.first(),
        false,
        subscribed = false,
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
      FakeLanguageService(),
      settingsDataStore,
      enableNotificationsReminderManager,
      NoopNetworkCacheManager(),
      uploadLanguagePreferenceToBackendUseCase = NoopUploadLanguagePreferenceToBackendUseCase(),
      changeEmailSubscriptionPreferencesUseCase = NoopChangeEmailSubscriptionPreferencesUseCase(),
    )

    settingsPresenter.test(
      SettingsUiState.Loaded(
        selectedLanguage = Language.entries.first(),
        languageOptions = Language.entries,
        selectedTheme = Theme.LIGHT,
        showNotificationReminder = false,
        subscribed = false,
      ),
    ) {
      assertThat(awaitItem().selectedTheme).isEqualTo(Theme.LIGHT)
      sendEvent(SettingsEvent.ChangeTheme(Theme.DARK))
      assertThat(awaitItem().selectedTheme).isEqualTo(Theme.DARK)
      sendEvent(SettingsEvent.ChangeTheme(Theme.DARK))
    }
  }
}

private class NoopNetworkCacheManager : NetworkCacheManager {
  override fun clearCache() {}
}

private class NoopUploadLanguagePreferenceToBackendUseCase : UploadLanguagePreferenceToBackendUseCase {
  override suspend fun invoke() {}
}

private class NoopChangeEmailSubscriptionPreferencesUseCase : ChangeEmailSubscriptionPreferencesUseCase {
  override suspend fun invoke(subscribe: Boolean): Either<SubPrefError, SubPrefSuccess> = either {
    return SubPrefSuccess.right()
  }
}
