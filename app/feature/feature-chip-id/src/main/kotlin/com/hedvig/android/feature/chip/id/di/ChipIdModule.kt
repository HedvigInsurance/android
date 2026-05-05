package com.hedvig.android.feature.chip.id.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.feature.chip.id.data.GetContractsWithMissingChipIdUseCase
import com.hedvig.android.feature.chip.id.data.GetContractsWithMissingChipIdUseCaseImpl
import com.hedvig.android.feature.chip.id.data.UpdateChipIdUseCase
import com.hedvig.android.feature.chip.id.data.UpdateChipIdUseCaseImpl
import com.hedvig.android.feature.chip.id.ui.AddChipIdViewModel
import com.hedvig.android.feature.chip.id.ui.selectinsurance.SelectInsuranceForChipIdViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chipIdModule = module {
  single<UpdateChipIdUseCase> {
    UpdateChipIdUseCaseImpl(
      apolloClient = get<ApolloClient>(),
    )
  }

  single<GetContractsWithMissingChipIdUseCase> {
    GetContractsWithMissingChipIdUseCaseImpl(
      apolloClient = get<ApolloClient>(),
    )
  }

  viewModel<SelectInsuranceForChipIdViewModel> { params ->
    SelectInsuranceForChipIdViewModel(
      preselectedContractId = params.getOrNull<String>(),
      getContractsWithMissingChipIdUseCase = get<GetContractsWithMissingChipIdUseCase>(),
    )
  }

  viewModel<AddChipIdViewModel> { params ->
    AddChipIdViewModel(
      updateChipIdUseCase = get<UpdateChipIdUseCase>(),
      contractId = params.get<String>(),
      getContractsWithMissingChipIdUseCase = get<GetContractsWithMissingChipIdUseCase>(),
    )
  }
}
