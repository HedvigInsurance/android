package com.hedvig.android.feature.help.center.di

import com.hedvig.android.feature.help.center.HelpCenterViewModel
import com.hedvig.android.featureflags.FeatureManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val helpCenterModule = module {
  viewModel<HelpCenterViewModel> {
    HelpCenterViewModel(
      featureManager = get<FeatureManager>(),
    )
  }
}
