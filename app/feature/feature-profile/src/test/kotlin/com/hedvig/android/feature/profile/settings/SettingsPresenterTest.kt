package com.hedvig.android.feature.profile.settings

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.auth.listeners.UploadLanguagePreferenceToBackendUseCase
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import com.hedvig.android.language.Language
import com.hedvig.android.language.test.FakeLanguageService
import com.hedvig.android.memberreminders.test.TestEnableNotificationsReminderManager
import com.hedvig.android.molecule.test.test
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class SettingsPresenterTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  @Test
  fun `content stays loading as long as notificationReminder are uninitialized`() = runTest {
    val enableNotificationsReminderManager = TestEnableNotificationsReminderManager()
    val settingsPresenter = SettingsPresenter(
      FakeLanguageService(),
      SettingsDataStore(TestPreferencesDataStore(testFolder, backgroundScope)),
      enableNotificationsReminderManager,
      NoopNetworkCacheManager(),
      NoopUploadLanguagePreferenceToBackendUseCase(),
      FakeFeatureManager2(false),
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
      SettingsDataStore(TestPreferencesDataStore(testFolder, backgroundScope)),
      enableNotificationsReminderManager,
      NoopNetworkCacheManager(),
      NoopUploadLanguagePreferenceToBackendUseCase(),
      FakeFeatureManager2(false),
    )

    settingsPresenter.test(
      SettingsUiState.Loaded(
        Language.EN_SE,
        listOf(Language.EN_SE, Language.SV_SE),
        Theme.SYSTEM_DEFAULT,
        false,
        SettingsUiState.ChatBubbleSetting.FeatureDisabled,
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
      SettingsDataStore(TestPreferencesDataStore(testFolder, backgroundScope)),
      enableNotificationsReminderManager,
      NoopNetworkCacheManager(),
      NoopUploadLanguagePreferenceToBackendUseCase(),
      FakeFeatureManager2(false),
    )

    settingsPresenter.test(
      SettingsUiState.Loaded(
        Language.EN_SE,
        listOf(Language.EN_SE, Language.SV_SE),
        Theme.SYSTEM_DEFAULT,
        false,
        SettingsUiState.ChatBubbleSetting.FeatureDisabled,
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
      SettingsDataStore(TestPreferencesDataStore(testFolder, backgroundScope)),
      enableNotificationsReminderManager,
      NoopNetworkCacheManager(),
      NoopUploadLanguagePreferenceToBackendUseCase(),
      FakeFeatureManager2(false),
    )

    settingsPresenter.test(
      SettingsUiState.Loaded(
        Language.entries.first(),
        Language.entries,
        Theme.entries.first(),
        false,
        SettingsUiState.ChatBubbleSetting.FeatureDisabled,
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
    val settingsDataStore = SettingsDataStore(TestPreferencesDataStore(testFolder, backgroundScope))
    val settingsPresenter = SettingsPresenter(
      FakeLanguageService(),
      settingsDataStore,
      TestEnableNotificationsReminderManager(),
      NoopNetworkCacheManager(),
      NoopUploadLanguagePreferenceToBackendUseCase(),
      FakeFeatureManager2(false),
    )

    settingsDataStore.setTheme(Theme.LIGHT)
    settingsPresenter.test(
      SettingsUiState.Loaded(
        selectedLanguage = Language.entries.first(),
        languageOptions = Language.entries,
        selectedTheme = Theme.LIGHT,
        showNotificationReminder = false,
        chatBubbleSetting = SettingsUiState.ChatBubbleSetting.FeatureDisabled,
      ),
    ) {
      assertThat(awaitItem().selectedTheme).isEqualTo(Theme.LIGHT)
      sendEvent(SettingsEvent.ChangeTheme(Theme.DARK))
      assertThat(awaitItem().selectedTheme).isEqualTo(Theme.DARK)
      sendEvent(SettingsEvent.ChangeTheme(Theme.LIGHT))
      assertThat(awaitItem().selectedTheme).isEqualTo(Theme.LIGHT)
      sendEvent(SettingsEvent.ChangeTheme(Theme.SYSTEM_DEFAULT))
      assertThat(awaitItem().selectedTheme).isEqualTo(Theme.SYSTEM_DEFAULT)
    }
  }

  @Test
  fun `Chat bubble setting respecs the feature flag and the user preference`(
    @TestParameter isFeatureFlagEnabled: Boolean,
    @TestParameter initialUserPreference: Boolean,
  ) = runTest {
    val settingsDataStore = SettingsDataStore(TestPreferencesDataStore(testFolder, backgroundScope))
    val settingsPresenter = SettingsPresenter(
      FakeLanguageService(),
      settingsDataStore,
      TestEnableNotificationsReminderManager(),
      NoopNetworkCacheManager(),
      NoopUploadLanguagePreferenceToBackendUseCase(),
      FakeFeatureManager2(mapOf(Feature.CHAT_BUBBLE to isFeatureFlagEnabled)),
    )
    settingsDataStore.setChatBubbleSetting(initialUserPreference)
    settingsPresenter.test(
      SettingsUiState.Loaded(
        selectedLanguage = Language.entries.first(),
        languageOptions = Language.entries,
        selectedTheme = null,
        showNotificationReminder = false,
        chatBubbleSetting = if (isFeatureFlagEnabled) {
          SettingsUiState.ChatBubbleSetting.FeatureEnabled(initialUserPreference)
        } else {
          SettingsUiState.ChatBubbleSetting.FeatureDisabled
        },
      ),
    ) {
      if (isFeatureFlagEnabled) {
        assertThat(awaitItem().chatBubbleSetting)
          .isEqualTo(SettingsUiState.ChatBubbleSetting.FeatureEnabled(initialUserPreference))
        expectNoEvents()
        sendEvent(SettingsEvent.SetChatBubblePreference(!initialUserPreference))
        assertThat(awaitItem().chatBubbleSetting)
          .isEqualTo(SettingsUiState.ChatBubbleSetting.FeatureEnabled(!initialUserPreference))
        sendEvent(SettingsEvent.SetChatBubblePreference(initialUserPreference))
        assertThat(awaitItem().chatBubbleSetting)
          .isEqualTo(SettingsUiState.ChatBubbleSetting.FeatureEnabled(initialUserPreference))
      } else {
        assertThat(awaitItem().chatBubbleSetting).isEqualTo(SettingsUiState.ChatBubbleSetting.FeatureDisabled)
        sendEvent(SettingsEvent.SetChatBubblePreference(true))
        expectNoEvents()
        sendEvent(SettingsEvent.SetChatBubblePreference(false))
        expectNoEvents()
      }
    }
  }
}

private class NoopNetworkCacheManager : NetworkCacheManager {
  override fun clearCache() {}
}

private class NoopUploadLanguagePreferenceToBackendUseCase : UploadLanguagePreferenceToBackendUseCase {
  override suspend fun invoke() {}
}
