package com.hedvig.android.feature.forever.di

import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.feature.forever.ForeverViewModel
import com.hedvig.android.feature.forever.data.GetReferralsInformationUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val foreverModule = module {
  viewModel {
    ForeverViewModel(
      get(),
      get(),
    )
  }
  single {
    GetReferralsInformationUseCase(
      get(giraffeClient),
      get(),
    )
  }
}
