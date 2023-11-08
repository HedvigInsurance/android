package com.hedvig.android.feature.forever.di

import com.hedvig.android.data.forever.ForeverRepositoryProvider
import com.hedvig.android.feature.forever.ForeverViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val foreverModule = module {
  viewModel<ForeverViewModel> {
    ForeverViewModel(
      get<ForeverRepositoryProvider>(),
    )
  }
}
