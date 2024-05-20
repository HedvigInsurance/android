package com.hedvig.android.feature.profile.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.auth.listeners.UploadLanguagePreferenceToBackendUseCase
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import com.hedvig.android.memberreminders.EnableNotificationsReminderManager
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.launch

internal class SettingsPresenter(
  private val languageService: LanguageService,
  private val settingsDataStore: SettingsDataStore,
  private val enableNotificationsReminderManager: EnableNotificationsReminderManager,
  private val cacheManager: NetworkCacheManager,
  private val uploadLanguagePreferenceToBackendUseCase: UploadLanguagePreferenceToBackendUseCase,
  private val featureManager: FeatureManager,
) : MoleculePresenter<SettingsEvent, SettingsUiState> {
  @Composable
  override fun MoleculePresenterScope<SettingsEvent>.present(lastState: SettingsUiState): SettingsUiState {
    var selectedLanguage by remember { mutableStateOf(lastState.selectedLanguage) }
    val selectedTheme = settingsDataStore.observeTheme().collectAsState(lastState.selectedTheme).value
    val showNotificationReminder = enableNotificationsReminderManager
      .showNotificationReminder()
      .collectAsState(lastState.showNotificationReminder)
      .value
    val chatBubbleSetting = chatBubbleSetting(settingsDataStore, featureManager, lastState)

    CollectEvents { event ->
      when (event) {
        is SettingsEvent.ChangeLanguage -> {
          selectedLanguage = event.language
          languageService.setLanguage(event.language)
          cacheManager.clearCache()
          launch { uploadLanguagePreferenceToBackendUseCase.invoke() }
        }

        is SettingsEvent.ChangeTheme -> {
          launch { settingsDataStore.setTheme(event.theme) }
        }

        SettingsEvent.SnoozeNotificationPermissionReminder -> {
          launch { enableNotificationsReminderManager.snoozeNotificationReminder() }
        }

        is SettingsEvent.SetChatBubblePreference -> {
          launch { settingsDataStore.setChatBubbleSetting(event.showChatBubble) }
        }
      }
    }

    return if (showNotificationReminder == null) {
      SettingsUiState.Loading(
        selectedLanguage = selectedLanguage,
        languageOptions = lastState.languageOptions,
      )
    } else {
      SettingsUiState.Loaded(
        selectedLanguage = selectedLanguage,
        languageOptions = lastState.languageOptions,
        selectedTheme = selectedTheme,
        showNotificationReminder = showNotificationReminder,
        chatBubbleSetting = chatBubbleSetting,
      )
    }
  }

  @Composable
  private fun chatBubbleSetting(
    settingsDataStore: SettingsDataStore,
    featureManager: FeatureManager,
    lastState: SettingsUiState,
  ): SettingsUiState.ChatBubbleSetting {
    val isFeatureEnabled by remember(featureManager) { featureManager.isFeatureEnabled(Feature.CHAT_BUBBLE) }
      .collectAsState(lastState.chatBubbleSetting != SettingsUiState.ChatBubbleSetting.FeatureDisabled)
    if (!isFeatureEnabled) {
      return SettingsUiState.ChatBubbleSetting.FeatureDisabled
    }
    val showChatAsBubble by remember(settingsDataStore) { settingsDataStore.chatBubbleSetting() }
      .collectAsState(
        lastState
          .chatBubbleSetting
          .safeCast<SettingsUiState.ChatBubbleSetting.FeatureEnabled>()
          ?.showChatAsBubble ?: false,
      )
    return SettingsUiState.ChatBubbleSetting.FeatureEnabled(showChatAsBubble)
  }
}

sealed interface SettingsUiState {
  val selectedLanguage: Language
  val languageOptions: List<Language>
  val selectedTheme: Theme?
  val showNotificationReminder: Boolean?
  val chatBubbleSetting: ChatBubbleSetting

  data class Loading(
    override val selectedLanguage: Language,
    override val languageOptions: List<Language>,
  ) : SettingsUiState {
    override val selectedTheme: Theme? = null
    override val showNotificationReminder: Boolean? = null
    override val chatBubbleSetting: ChatBubbleSetting = ChatBubbleSetting.FeatureDisabled
  }

  data class Loaded(
    override val selectedLanguage: Language,
    override val languageOptions: List<Language>,
    override val selectedTheme: Theme?,
    override val showNotificationReminder: Boolean,
    override val chatBubbleSetting: ChatBubbleSetting,
  ) : SettingsUiState

  sealed interface ChatBubbleSetting {
    data object FeatureDisabled : ChatBubbleSetting

    data class FeatureEnabled(val showChatAsBubble: Boolean) : ChatBubbleSetting
  }
}

sealed interface SettingsEvent {
  data class ChangeLanguage(val language: Language) : SettingsEvent

  data class ChangeTheme(val theme: Theme) : SettingsEvent

  data class SetChatBubblePreference(val showChatBubble: Boolean) : SettingsEvent

  data object SnoozeNotificationPermissionReminder : SettingsEvent
}
