package com.hedvig.android.feature.profile.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.auth.listeners.UploadLanguagePreferenceToBackendUseCase
import com.hedvig.android.data.chat.icon.ChatIconAppState
import com.hedvig.android.data.chat.icon.GetChatIconAppStateUseCase
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import com.hedvig.android.memberreminders.EnableNotificationsReminderManager
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class SettingsPresenter(
  private val languageService: LanguageService,
  private val settingsDataStore: SettingsDataStore,
  private val enableNotificationsReminderManager: EnableNotificationsReminderManager,
  private val cacheManager: NetworkCacheManager,
  private val uploadLanguagePreferenceToBackendUseCase: UploadLanguagePreferenceToBackendUseCase,
  private val getChatIconAppStateUseCase: GetChatIconAppStateUseCase,
) : MoleculePresenter<SettingsEvent, SettingsUiState> {
  @Composable
  override fun MoleculePresenterScope<SettingsEvent>.present(lastState: SettingsUiState): SettingsUiState {
    var selectedLanguage by remember { mutableStateOf(lastState.selectedLanguage) }
    val selectedTheme = settingsDataStore.observeTheme().collectAsState(lastState.selectedTheme).value
    val showNotificationReminder = enableNotificationsReminderManager
      .showNotificationReminder()
      .collectAsState(lastState.showNotificationReminder)
      .value
    val chatBubbleSettingState: SettingsUiState.ChatBubbleSettingState by remember(getChatIconAppStateUseCase) {
      getChatIconAppStateUseCase.invoke().map { it.toChatBubbleSettingState() }
    }.collectAsState(lastState.chatBubbleSettingState)

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
        chatBubbleSettingState = chatBubbleSettingState,
      )
    }
  }
}

internal sealed interface SettingsUiState {
  val selectedLanguage: Language
  val languageOptions: List<Language>
  val selectedTheme: Theme?
  val showNotificationReminder: Boolean?
  val chatBubbleSettingState: ChatBubbleSettingState

  data class Loading(
    override val selectedLanguage: Language,
    override val languageOptions: List<Language>,
  ) : SettingsUiState {
    override val selectedTheme: Theme? = null
    override val showNotificationReminder: Boolean? = null
    override val chatBubbleSettingState: ChatBubbleSettingState = ChatBubbleSettingState.SettingNotAvailable
  }

  data class Loaded(
    override val selectedLanguage: Language,
    override val languageOptions: List<Language>,
    override val selectedTheme: Theme?,
    override val showNotificationReminder: Boolean,
    override val chatBubbleSettingState: ChatBubbleSettingState,
  ) : SettingsUiState

  sealed interface ChatBubbleSettingState {
    data object SettingNotAvailable : ChatBubbleSettingState

    data class SettingVisible(val showAsBubbleSetting: Boolean) : ChatBubbleSettingState
  }
}

internal sealed interface SettingsEvent {
  data class ChangeLanguage(val language: Language) : SettingsEvent

  data class ChangeTheme(val theme: Theme) : SettingsEvent

  data class SetChatBubblePreference(val showChatBubble: Boolean) : SettingsEvent

  data object SnoozeNotificationPermissionReminder : SettingsEvent
}

private fun ChatIconAppState.toChatBubbleSettingState(): SettingsUiState.ChatBubbleSettingState {
  return when (this) {
    ChatIconAppState.Hidden -> SettingsUiState.ChatBubbleSettingState.SettingNotAvailable
    is ChatIconAppState.ShownAlwaysPinned -> SettingsUiState.ChatBubbleSettingState.SettingNotAvailable
    is ChatIconAppState.ShownAndCanFloat -> SettingsUiState.ChatBubbleSettingState.SettingVisible(showAsFloatingBubble)
  }
}
