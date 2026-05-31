package com.hedvig.android.feature.profile.settings

import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.auth.listeners.UploadLanguagePreferenceToBackendUseCase
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.feature.profile.data.ChangeEmailSubscriptionPreferencesUseCase
import com.hedvig.android.language.LanguageService
import com.hedvig.android.memberreminders.EnableNotificationsReminderSnoozeManager
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class SettingsViewModel(
  languageService: LanguageService,
  settingsDataStore: SettingsDataStore,
  changeEmailSubscriptionPreferencesUseCase: ChangeEmailSubscriptionPreferencesUseCase,
  enableNotificationsReminderSnoozeManager: EnableNotificationsReminderSnoozeManager,
  cacheManager: NetworkCacheManager,
  uploadLanguagePreferenceToBackendUseCase: UploadLanguagePreferenceToBackendUseCase,
) : MoleculeViewModel<SettingsEvent, SettingsUiState>(
    SettingsUiState.Loading(selectedLanguage = languageService.getLanguage()),
    SettingsPresenter(
      languageService = languageService,
      settingsDataStore = settingsDataStore,
      enableNotificationsReminderSnoozeManager = enableNotificationsReminderSnoozeManager,
      cacheManager = cacheManager,
      uploadLanguagePreferenceToBackendUseCase = uploadLanguagePreferenceToBackendUseCase,
      changeEmailSubscriptionPreferencesUseCase = changeEmailSubscriptionPreferencesUseCase,
    ),
  )
