@file:Suppress("RemoveExplicitTypeArguments")

package com.hedvig.android.feature.businessmodel.di

import com.hedvig.android.feature.businessmodel.BusinessModelViewModel
import com.hedvig.android.feature.businessmodel.GetBusinessModelInformationUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val businessModelModule = module {
  single<GetBusinessModelInformationUseCase> { GetBusinessModelInformationUseCase(get()) }
  viewModel<BusinessModelViewModel> { BusinessModelViewModel(get(), get()) }
}
