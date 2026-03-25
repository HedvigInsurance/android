package com.hedvig.android.feature.chip.id.di

import com.hedvig.android.feature.chip.id.ui.AddChipIdViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chipIdModule = module {
  viewModel<AddChipIdViewModel> {
    AddChipIdViewModel()
  }
}
