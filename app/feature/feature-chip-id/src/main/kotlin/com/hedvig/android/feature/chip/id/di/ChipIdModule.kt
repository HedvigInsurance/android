package com.hedvig.android.feature.chip.id.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.feature.chip.id.data.UpdateChipIdUseCase
import com.hedvig.android.feature.chip.id.data.UpdateChipIdUseCaseImpl
import com.hedvig.android.feature.chip.id.ui.AddChipIdViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chipIdModule = module {
  single<UpdateChipIdUseCase> {
    UpdateChipIdUseCaseImpl(
      apolloClient = get<ApolloClient>(),
    )
  }

  viewModel<AddChipIdViewModel> { params ->
    AddChipIdViewModel(
      updateChipIdUseCase = get<UpdateChipIdUseCase>(),
      insuranceId = params.get<String>(),
    )
  }
}
