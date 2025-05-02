package com.hedvig.android.feature.profile.settings

import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.auth.listeners.UploadLanguagePreferenceToBackendUseCase
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.feature.profile.data.ChangeEmailSubscriptionPreferencesUseCase
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.android.memberreminders.EnableNotificationsReminderSnoozeManager
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class SettingsViewModel(
  marketManager: MarketManager,
  languageService: LanguageService,
  settingsDataStore: SettingsDataStore,
  changeEmailSubscriptionPreferencesUseCase: ChangeEmailSubscriptionPreferencesUseCase,
  enableNotificationsReminderSnoozeManager: EnableNotificationsReminderSnoozeManager,
  cacheManager: NetworkCacheManager,
  uploadLanguagePreferenceToBackendUseCase: UploadLanguagePreferenceToBackendUseCase,
) : MoleculeViewModel<SettingsEvent, SettingsUiState>(
    SettingsUiState.Loading(
      selectedLanguage = languageService.getLanguage(),
      // TODO: MarketCleanup
      showEmailSubscriptionPreferences = marketManager.market.value == Market.SE,
      languageOptions = Market.SE.availableLanguages,
    ),
    SettingsPresenter(
      languageService = languageService,
      settingsDataStore = settingsDataStore,
      enableNotificationsReminderSnoozeManager = enableNotificationsReminderSnoozeManager,
      cacheManager = cacheManager,
      uploadLanguagePreferenceToBackendUseCase = uploadLanguagePreferenceToBackendUseCase,
      changeEmailSubscriptionPreferencesUseCase = changeEmailSubscriptionPreferencesUseCase,
      isSwedishMarket = marketManager.market.value == Market.SE,
    ),
  )
