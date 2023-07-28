package com.hedvig.android.feature.forever.di

import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.feature.forever.GetReferralsInformationUseCase
import com.hedvig.android.feature.forever.ReferralsViewModel
import com.hedvig.android.feature.forever.data.ReferralsRepository
import com.hedvig.android.language.LanguageService
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val foreverModule = module {
  viewModel<ReferralsViewModel> {
    ReferralsViewModel(
      get<ReferralsRepository>(),
      get<GetReferralsInformationUseCase>(),
      get<LanguageService>(),
    )
  }
  single<GetReferralsInformationUseCase> {
    GetReferralsInformationUseCase(
      get(giraffeClient),
      get()
    )
  }
}
