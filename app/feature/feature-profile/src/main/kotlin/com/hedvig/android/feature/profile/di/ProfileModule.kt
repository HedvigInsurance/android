package com.hedvig.android.feature.profile.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.auth.listeners.UploadLanguagePreferenceToBackendUseCase
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.feature.profile.data.CheckTravelCertificateDestinationAvailabilityUseCase
import com.hedvig.android.feature.profile.data.CheckTravelCertificateDestinationAvailabilityUseCaseImpl
import com.hedvig.android.feature.profile.aboutapp.AboutAppViewModel
import com.hedvig.android.feature.profile.data.ProfileRepositoryDemo
import com.hedvig.android.feature.profile.data.ProfileRepositoryImpl
import com.hedvig.android.feature.profile.eurobonus.EurobonusViewModel
import com.hedvig.android.feature.profile.myinfo.MyInfoViewModel
import com.hedvig.android.feature.profile.settings.SettingsViewModel
import com.hedvig.android.feature.profile.tab.GetEurobonusStatusUseCase
import com.hedvig.android.feature.profile.tab.NetworkGetEurobonusStatusUseCase
import com.hedvig.android.feature.profile.tab.ProfileViewModel
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager
import com.hedvig.android.memberreminders.EnableNotificationsReminderManager
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
  single<GetEurobonusStatusUseCase> { NetworkGetEurobonusStatusUseCase(get<ApolloClient>()) }
  viewModel<ProfileViewModel> {
    ProfileViewModel(
      get<GetEurobonusStatusUseCase>(),
      get<CheckTravelCertificateDestinationAvailabilityUseCase>(),
      get<GetMemberRemindersUseCase>(),
      get<EnableNotificationsReminderManager>(),
      get<FeatureManager>(),
      get<LogoutUseCase>(),
    )
  }
  viewModel<EurobonusViewModel> { EurobonusViewModel(get<ApolloClient>()) }

  single<ProfileRepositoryImpl> {
    ProfileRepositoryImpl(
      apolloClient = get<ApolloClient>(),
      networkCacheManager = get<NetworkCacheManager>(),
    )
  }
  single<ProfileRepositoryDemo> {
    ProfileRepositoryDemo()
  }
  single<ProfileRepositoryProvider> {
    ProfileRepositoryProvider(
      demoManager = get<DemoManager>(),
      prodImpl = get<ProfileRepositoryImpl>(),
      demoImpl = get<ProfileRepositoryDemo>(),
    )
  }

  viewModel<SettingsViewModel> {
    SettingsViewModel(
      marketManager = get<MarketManager>(),
      languageService = get<LanguageService>(),
      settingsDataStore = get<SettingsDataStore>(),
      enableNotificationsReminderManager = get<EnableNotificationsReminderManager>(),
      cacheManager = get<NetworkCacheManager>(),
      uploadLanguagePreferenceToBackendUseCase = get<UploadLanguagePreferenceToBackendUseCase>(),
    )
  }

  viewModel<MyInfoViewModel> {
    MyInfoViewModel(get<ProfileRepositoryProvider>())
  }
  viewModel<AboutAppViewModel> { AboutAppViewModel(get<ApolloClient>()) }
  single<CheckTravelCertificateDestinationAvailabilityUseCase> {
    CheckTravelCertificateDestinationAvailabilityUseCaseImpl(
      get<ApolloClient>(),
    )
  }
}
