@file:Suppress("RemoveExplicitTypeArguments")

package com.hedvig.android.feature.businessmodel.di

import com.hedvig.android.feature.businessmodel.BusinessModelViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val businessModelModule = module {
  viewModel<BusinessModelViewModel> { BusinessModelViewModel(get()) }
}
