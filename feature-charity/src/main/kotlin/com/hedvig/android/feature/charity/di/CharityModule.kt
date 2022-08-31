@file:Suppress("RemoveExplicitTypeArguments")

package com.hedvig.android.feature.charity.di

import com.hedvig.android.feature.charity.CharityViewModel
import com.hedvig.android.feature.charity.GetCharityInformationUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val charityModule = module {
  single<GetCharityInformationUseCase> { GetCharityInformationUseCase(get()) }
  viewModel<CharityViewModel> { CharityViewModel(get(), get()) }
}
