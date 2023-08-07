package com.hedvig.android.feature.profile.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.feature.profile.aboutapp.AboutAppViewModel
import com.hedvig.android.feature.profile.data.ProfileRepository
import com.hedvig.android.feature.profile.data.ProfileRepositoryImpl
import com.hedvig.android.feature.profile.eurobonus.EurobonusViewModel
import com.hedvig.android.feature.profile.myinfo.MyInfoViewModel
import com.hedvig.android.feature.profile.payment.PaymentViewModel
import com.hedvig.android.feature.profile.payment.history.PaymentHistoryViewModel
import com.hedvig.android.feature.profile.settings.ChangeLanguageUseCase
import com.hedvig.android.feature.profile.settings.SettingsViewModel
import com.hedvig.android.feature.profile.tab.GetEurobonusStatusUseCase
import com.hedvig.android.feature.profile.tab.NetworkGetEurobonusStatusUseCase
import com.hedvig.android.feature.profile.tab.ProfileViewModel
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.language.LanguageService
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
  single<ProfileRepository> {
    ProfileRepositoryImpl(
      giraffeApolloClient = get<ApolloClient>(giraffeClient),
      octopusApolloClient = get<ApolloClient>(octopusClient),
    )
  }
  single<GetEurobonusStatusUseCase> { NetworkGetEurobonusStatusUseCase(get<ApolloClient>(octopusClient)) }
  viewModel<ProfileViewModel> {
    ProfileViewModel(
      get<GetEurobonusStatusUseCase>(),
      get<GetMemberRemindersUseCase>(),
      get<FeatureManager>(),
      get<LogoutUseCase>(),
    )
  }
  viewModel<EurobonusViewModel> { EurobonusViewModel(get<ApolloClient>(octopusClient)) }

  single<ChangeLanguageUseCase> {
    ChangeLanguageUseCase(
      apolloClient = get<ApolloClient>(giraffeClient),
      languageService = get<LanguageService>(),
      cacheManager = get<NetworkCacheManager>(),
    )
  }

  viewModel<SettingsViewModel> {
    SettingsViewModel(
      hAnalytics = get(),
      changeLanguageUseCase = get(),
      marketManager = get(),
      languageService = get(),
      settingsDataStore = get(),
    )
  }

  viewModel<MyInfoViewModel> { MyInfoViewModel(get(), get()) }
  viewModel<AboutAppViewModel> { AboutAppViewModel(get(), get<ApolloClient>(giraffeClient)) }

  viewModel<PaymentViewModel> { PaymentViewModel(get(), get(), get()) }
  viewModel<PaymentHistoryViewModel> { PaymentHistoryViewModel(get(), get()) }
}
