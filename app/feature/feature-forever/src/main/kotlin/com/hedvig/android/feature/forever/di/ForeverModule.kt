package com.hedvig.android.feature.forever.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.data.forever.di.ForeverRepositoryProvider
import com.hedvig.android.feature.forever.ForeverViewModel
import com.hedvig.android.feature.forever.data.GetReferralsInformationUseCaseDemo
import com.hedvig.android.feature.forever.data.GetReferralsInformationUseCaseImpl
import com.hedvig.android.language.LanguageService
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val foreverModule = module {
  viewModel<ForeverViewModel> {
    ForeverViewModel(
      get<ForeverRepositoryProvider>(),
      get<GetReferralsInformationUseCaseProvider>(),
    )
  }
  single<GetReferralsInformationUseCaseImpl> {
    GetReferralsInformationUseCaseImpl(
      get<ApolloClient>(giraffeClient),
      get<LanguageService>(),
    )
  }
  single<GetReferralsInformationUseCaseDemo> {
    GetReferralsInformationUseCaseDemo()
  }
  single {
    GetReferralsInformationUseCaseProvider(
      demoManager = get(),
      prodImpl = get<GetReferralsInformationUseCaseImpl>(),
      demoImpl = get<GetReferralsInformationUseCaseDemo>(),
    )
  }
}
