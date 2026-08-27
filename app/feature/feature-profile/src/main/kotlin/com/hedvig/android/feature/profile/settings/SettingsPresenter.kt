package com.hedvig.android.feature.profile.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.auth.listeners.UploadLanguagePreferenceToBackendUseCase
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.data.settings.datastore.GetAnalyticsConsentUseCase
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.feature.profile.data.ChangeEmailSubscriptionPreferencesUseCase
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import com.hedvig.android.memberreminders.EnableNotificationsReminderSnoozeManager
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.launch

internal class SettingsPresenter(
  private val languageService: LanguageService,
  private val settingsDataStore: SettingsDataStore,
  private val enableNotificationsReminderSnoozeManager: EnableNotificationsReminderSnoozeManager,
  private val cacheManager: NetworkCacheManager,
  private val changeEmailSubscriptionPreferencesUseCase: ChangeEmailSubscriptionPreferencesUseCase,
  private val uploadLanguagePreferenceToBackendUseCase: UploadLanguagePreferenceToBackendUseCase,
  private val getAnalyticsConsentUseCase: GetAnalyticsConsentUseCase,
) : MoleculePresenter<SettingsEvent, SettingsUiState> {
  @Composable
  override fun MoleculePresenterScope<SettingsEvent>.present(lastState: SettingsUiState): SettingsUiState {
    var selectedLanguage by remember { mutableStateOf(lastState.selectedLanguage) }
    var emailSubscriptionPreferenceError by remember { mutableStateOf(false) }
    val selectedTheme = settingsDataStore.observeTheme().collectAsState(lastState.selectedTheme).value
    val isSubscribedToEmails = settingsDataStore.observeEmailSubscriptionPreference().collectAsState(
      lastState.isSubscribedToEmails,
    ).value
    val showNotificationReminder = enableNotificationsReminderSnoozeManager
      .timeToShowNotificationReminder()
      .collectAsState(lastState.showNotificationReminder)
      .value
    val analyticsConsent = getAnalyticsConsentUseCase.invoke()
      .collectAsState(lastState.analyticsConsent).value

    CollectEvents { event ->
      when (event) {
        is SettingsEvent.ChangeLanguage -> {
          selectedLanguage = event.language
          languageService.setLanguage(event.language)
          launch {
            cacheManager.clearCache()
            uploadLanguagePreferenceToBackendUseCase.invoke()
          }
        }

        is SettingsEvent.ChangeTheme -> {
          launch { settingsDataStore.setTheme(event.theme) }
        }

        SettingsEvent.SnoozeNotificationPermissionReminder -> {
          launch { enableNotificationsReminderSnoozeManager.snoozeNotificationReminder() }
        }

        is SettingsEvent.ChangeSubscriptionPreference -> {
          launch {
            changeEmailSubscriptionPreferencesUseCase.invoke(event.subscribe)
              .onLeft {
                emailSubscriptionPreferenceError = true
              }
              .onRight {
                emailSubscriptionPreferenceError = false
                settingsDataStore.setEmailSubscriptionPreference(event.subscribe)
              }
          }
        }
      }
    }

    return if (showNotificationReminder == null) {
      SettingsUiState.Loading(
        selectedLanguage = selectedLanguage,
      )
    } else {
      SettingsUiState.Loaded(
        selectedLanguage = selectedLanguage,
        languageOptions = lastState.languageOptions,
        selectedTheme = selectedTheme,
        showNotificationReminder = showNotificationReminder,
        isSubscribedToEmails = isSubscribedToEmails,
        emailSubscriptionPreferenceError = emailSubscriptionPreferenceError,
        analyticsConsent = analyticsConsent,
      )
    }
  }
}

sealed interface SettingsUiState {
  val selectedLanguage: Language
  val selectedTheme: Theme?
  val isSubscribedToEmails: Boolean?
  val showNotificationReminder: Boolean?

  /** Null while analytics consent is switched off, in which case the usage data row is not offered. */
  val analyticsConsent: AnalyticsConsent?
  val languageOptions: List<Language>
    get() = Language.entries

  data class Loading(
    override val selectedLanguage: Language,
  ) : SettingsUiState {
    override val isSubscribedToEmails: Boolean? = null
    override val selectedTheme: Theme? = null
    override val showNotificationReminder: Boolean? = null
    override val analyticsConsent: AnalyticsConsent? = null
  }

  data class Loaded(
    override val selectedLanguage: Language,
    override val languageOptions: List<Language>,
    override val selectedTheme: Theme?,
    override val showNotificationReminder: Boolean,
    override val isSubscribedToEmails: Boolean?,
    val emailSubscriptionPreferenceError: Boolean = false,
    override val analyticsConsent: AnalyticsConsent?,
  ) : SettingsUiState
}

sealed interface SettingsEvent {
  data class ChangeLanguage(val language: Language) : SettingsEvent

  data class ChangeTheme(val theme: Theme) : SettingsEvent

  data class ChangeSubscriptionPreference(val subscribe: Boolean) : SettingsEvent

  data object SnoozeNotificationPermissionReminder : SettingsEvent
}
