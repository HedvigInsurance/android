package com.hedvig.android.feature.profile.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.auth.listeners.UploadLanguagePreferenceToBackendUseCase
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.core.datastore.DeviceIdDataStore
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.feature.profile.aboutapp.AboutAppViewModel
import com.hedvig.android.feature.profile.certificates.CertificatesViewModel
import com.hedvig.android.feature.profile.contactinfo.ContactInfoViewModel
import com.hedvig.android.feature.profile.data.ChangeEmailSubscriptionPreferencesUseCase
import com.hedvig.android.feature.profile.data.ChangeEmailSubscriptionPreferencesUseCaseImpl
import com.hedvig.android.feature.profile.data.CheckCertificatesAvailabilityUseCase
import com.hedvig.android.feature.profile.data.CheckCertificatesAvailabilityUseCaseImpl
import com.hedvig.android.feature.profile.data.CheckInsuranceEvidenceAvailabilityUseCase
import com.hedvig.android.feature.profile.data.CheckInsuranceEvidenceAvailabilityUseCaseImpl
import com.hedvig.android.feature.profile.data.CheckTravelCertificateDestinationAvailabilityUseCase
import com.hedvig.android.feature.profile.data.CheckTravelCertificateDestinationAvailabilityUseCaseImpl
import com.hedvig.android.feature.profile.data.ContactInfoRepositoryDemo
import com.hedvig.android.feature.profile.data.ContactInfoRepositoryImpl
import com.hedvig.android.feature.profile.data.GetEurobonusDataUseCase
import com.hedvig.android.feature.profile.data.GetEurobonusDataUseCaseImpl
import com.hedvig.android.feature.profile.data.UpdateEurobonusNumberUseCase
import com.hedvig.android.feature.profile.data.UpdateEurobonusNumberUseCaseImpl
import com.hedvig.android.feature.profile.eurobonus.EurobonusViewModel
import com.hedvig.android.feature.profile.settings.SettingsViewModel
import com.hedvig.android.feature.profile.tab.GetEurobonusStatusUseCase
import com.hedvig.android.feature.profile.tab.NetworkGetEurobonusStatusUseCase
import com.hedvig.android.feature.profile.tab.ProfileViewModel
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.language.LanguageService
import com.hedvig.android.memberreminders.EnableNotificationsReminderSnoozeManager
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
  single<GetEurobonusStatusUseCase> { NetworkGetEurobonusStatusUseCase(get<ApolloClient>()) }
  single<CheckCertificatesAvailabilityUseCase> {
    CheckCertificatesAvailabilityUseCaseImpl(
      get<CheckTravelCertificateDestinationAvailabilityUseCase>(),
      get<CheckInsuranceEvidenceAvailabilityUseCase>(),
    )
  }
  viewModel<ProfileViewModel> {
    ProfileViewModel(
      get<GetEurobonusStatusUseCase>(),
      get<CheckCertificatesAvailabilityUseCase>(),
      get<GetMemberRemindersUseCase>(),
      get<EnableNotificationsReminderSnoozeManager>(),
      get<FeatureManager>(),
      get<LogoutUseCase>(),
    )
  }
  viewModel<EurobonusViewModel> {
    EurobonusViewModel(
      getEurobonusDataUseCase = get<GetEurobonusDataUseCase>(),
      updateEurobonusNumberUseCase = get<UpdateEurobonusNumberUseCase>(),
    )
  }
  single<CheckInsuranceEvidenceAvailabilityUseCase> {
    CheckInsuranceEvidenceAvailabilityUseCaseImpl(get<ApolloClient>())
  }
  viewModel<CertificatesViewModel> {
    CertificatesViewModel(
      get<CheckTravelCertificateDestinationAvailabilityUseCase>(),
      get<CheckInsuranceEvidenceAvailabilityUseCase>(),
    )
  }

  single<GetEurobonusDataUseCase> {
    GetEurobonusDataUseCaseImpl(apolloClient = get<ApolloClient>())
  }

  single<UpdateEurobonusNumberUseCase> {
    UpdateEurobonusNumberUseCaseImpl(apolloClient = get<ApolloClient>())
  }

  single<ChangeEmailSubscriptionPreferencesUseCase> {
    ChangeEmailSubscriptionPreferencesUseCaseImpl(
      apolloClient = get<ApolloClient>(),
    )
  }
  single<ContactInfoRepositoryImpl> {
    ContactInfoRepositoryImpl(
      apolloClient = get<ApolloClient>(),
      networkCacheManager = get<NetworkCacheManager>(),
    )
  }
  single<ContactInfoRepositoryDemo> {
    ContactInfoRepositoryDemo()
  }
  single<ContactInfoRepositoryImpl> {
    ContactInfoRepositoryImpl(
      apolloClient = get<ApolloClient>(),
      networkCacheManager = get<NetworkCacheManager>(),
    )
  }
  single<ContactInfoRepositoryDemo> {
    ContactInfoRepositoryDemo()
  }
  single<ProfileRepositoryProvider> {
    ProfileRepositoryProvider(
      demoManager = get<DemoManager>(),
      prodImpl = get<ContactInfoRepositoryImpl>(),
      demoImpl = get<ContactInfoRepositoryDemo>(),
    )
  }

  viewModel<SettingsViewModel> {
    SettingsViewModel(
      languageService = get<LanguageService>(),
      settingsDataStore = get<SettingsDataStore>(),
      enableNotificationsReminderSnoozeManager = get<EnableNotificationsReminderSnoozeManager>(),
      cacheManager = get<NetworkCacheManager>(),
      uploadLanguagePreferenceToBackendUseCase = get<UploadLanguagePreferenceToBackendUseCase>(),
      changeEmailSubscriptionPreferencesUseCase = get<ChangeEmailSubscriptionPreferencesUseCase>(),
    )
  }

  viewModel<ContactInfoViewModel> {
    ContactInfoViewModel(get<ProfileRepositoryProvider>())
  }
  viewModel<AboutAppViewModel> {
    AboutAppViewModel(get<ApolloClient>(), get<DeviceIdDataStore>())
  }
  single<CheckTravelCertificateDestinationAvailabilityUseCase> {
    CheckTravelCertificateDestinationAvailabilityUseCaseImpl(
      get<ApolloClient>(),
    )
  }
}
