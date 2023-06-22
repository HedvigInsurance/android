package com.hedvig.android.feature.legacyclaimtriaging.di

import com.hedvig.android.feature.legacyclaimtriaging.LegacyClaimTriagingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val legacyClaimTriagingModule = module {
  viewModel<LegacyClaimTriagingViewModel> { LegacyClaimTriagingViewModel(get()) }
}
