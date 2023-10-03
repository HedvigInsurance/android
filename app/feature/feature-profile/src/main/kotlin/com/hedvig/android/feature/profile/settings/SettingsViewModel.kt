package com.hedvig.android.feature.profile.settings

import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.android.memberreminders.EnableNotificationsReminderManager
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics

internal class SettingsViewModel(
  hAnalytics: HAnalytics,
  notifyBackendAboutLanguageChangeUseCase: NotifyBackendAboutLanguageChangeUseCase,
  marketManager: MarketManager,
  languageService: LanguageService,
  settingsDataStore: SettingsDataStore,
  enableNotificationsReminderManager: EnableNotificationsReminderManager,
  featureManager: FeatureManager,
) : MoleculeViewModel<SettingsEvent, SettingsUiState>(
  SettingsUiState.Loading(
    selectedLanguage = languageService.getLanguage(),
    languageOptions = when (marketManager.market.value) {
      Market.SE -> listOf(Language.EN_SE, Language.SV_SE)
      Market.NO -> listOf(Language.EN_NO, Language.NB_NO)
      Market.DK -> listOf(Language.EN_DK, Language.DA_DK)
    },
  ),
  SettingsPresenter(
    notifyBackendAboutLanguageChangeUseCase = notifyBackendAboutLanguageChangeUseCase,
    languageService = languageService,
    settingsDataStore = settingsDataStore,
    enableNotificationsReminderManager = enableNotificationsReminderManager,
    featureManager = featureManager,
  ),
) {
  init {
    hAnalytics.screenView(AppScreen.APP_SETTINGS)
  }
}
