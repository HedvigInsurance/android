package com.hedvig.android.feature.profile.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.datastore.SettingsDataStore
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.android.memberreminders.EnableNotificationsReminderManager
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.theme.Theme
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics

internal class SettingsViewModel(
  hAnalytics: HAnalytics,
  notifyBackendAboutLanguageChangeUseCase: NotifyBackendAboutLanguageChangeUseCase,
  marketManager: MarketManager,
  languageService: LanguageService,
  settingsDataStore: SettingsDataStore,
  enableNotificationsReminderManager: EnableNotificationsReminderManager,
) : MoleculeViewModel<SettingsEvent, SettingsUiState>(
  SettingsUiState.Loading(
    selectedLanguage = languageService.getLanguage(),
    themeOptions = Theme.values().toList(),
    languageOptions = when (marketManager.market) {
      Market.SE -> listOf(Language.EN_SE, Language.SV_SE)
      Market.NO -> listOf(Language.EN_NO, Language.NB_NO)
      Market.DK -> listOf(Language.EN_DK, Language.DA_DK)
      Market.FR -> listOf(Language.EN_FR, Language.FR_FR)
      null -> listOf(Language.EN_SE, Language.SV_SE)
    },
  ),
  SettingsPresenter(
    notifyBackendAboutLanguageChangeUseCase = notifyBackendAboutLanguageChangeUseCase,
    languageService = languageService,
    settingsDataStore = settingsDataStore,
    enableNotificationsReminderManager = enableNotificationsReminderManager,
  ),
) {
  init {
    hAnalytics.screenView(AppScreen.APP_SETTINGS)
  }
}

internal class SettingsPresenter(
  private val notifyBackendAboutLanguageChangeUseCase: NotifyBackendAboutLanguageChangeUseCase,
  private val languageService: LanguageService,
  private val settingsDataStore: SettingsDataStore,
  private val enableNotificationsReminderManager: EnableNotificationsReminderManager,
) : MoleculePresenter<SettingsEvent, SettingsUiState> {
  @Composable
  override fun MoleculePresenterScope<SettingsEvent>.present(lastState: SettingsUiState): SettingsUiState {
    var selectedLanguage by remember { mutableStateOf(lastState.selectedLanguage) }
    val selectedTheme = settingsDataStore.observeTheme().collectAsState(lastState.selectedTheme).value
    val showNotificationReminder = enableNotificationsReminderManager
      .showNotificationReminder()
      .collectAsState(lastState.showNotificationReminder)
      .value

    CollectEvents { event ->
      when (event) {
        is SettingsEvent.ChangeLanguage -> {
          languageService.setLanguage(event.language)
          notifyBackendAboutLanguageChangeUseCase.invoke(event.language)
          selectedLanguage = event.language
        }
        is SettingsEvent.ChangeTheme -> {
          settingsDataStore.setTheme(event.theme)
        }
        SettingsEvent.SnoozeNotificationReminder -> {
          enableNotificationsReminderManager.snoozeNotificationReminder()
        }
      }
    }

    return if (selectedTheme == null || showNotificationReminder == null) {
      SettingsUiState.Loading(
        selectedLanguage = selectedLanguage,
        themeOptions = lastState.themeOptions,
        languageOptions = lastState.languageOptions,
      )
    } else {
      SettingsUiState.Loaded(
        selectedLanguage = selectedLanguage,
        themeOptions = lastState.themeOptions,
        languageOptions = lastState.languageOptions,
        selectedTheme = selectedTheme,
        showNotificationReminder = showNotificationReminder,
      )
    }
  }
}

sealed interface SettingsUiState {
  val selectedLanguage: Language
  val themeOptions: List<Theme>
  val languageOptions: List<Language>
  val selectedTheme: Theme?
  val showNotificationReminder: Boolean?

  data class Loading(
    override val selectedLanguage: Language,
    override val themeOptions: List<Theme>,
    override val languageOptions: List<Language>,
  ) : SettingsUiState {
    override val selectedTheme: Theme? = null
    override val showNotificationReminder: Boolean? = null
  }

  data class Loaded(
    override val selectedLanguage: Language,
    override val themeOptions: List<Theme>,
    override val languageOptions: List<Language>,
    override val selectedTheme: Theme,
    override val showNotificationReminder: Boolean,
  ) : SettingsUiState
}

sealed interface SettingsEvent {
  data class ChangeLanguage(val language: Language) : SettingsEvent
  data class ChangeTheme(val theme: Theme) : SettingsEvent
  object SnoozeNotificationReminder : SettingsEvent
}
