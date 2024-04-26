package com.hedvig.android.feature.profile.settings

import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.auth.listeners.UploadLanguagePreferenceToBackendUseCase
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.android.memberreminders.EnableNotificationsReminderManager
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class SettingsViewModel(
  marketManager: MarketManager,
  languageService: LanguageService,
  settingsDataStore: SettingsDataStore,
  enableNotificationsReminderManager: EnableNotificationsReminderManager,
  cacheManager: NetworkCacheManager,
  uploadLanguagePreferenceToBackendUseCase: UploadLanguagePreferenceToBackendUseCase,
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
      languageService = languageService,
      settingsDataStore = settingsDataStore,
      enableNotificationsReminderManager = enableNotificationsReminderManager,
      cacheManager = cacheManager,
      uploadLanguagePreferenceToBackendUseCase = uploadLanguagePreferenceToBackendUseCase,
    ),
  )
