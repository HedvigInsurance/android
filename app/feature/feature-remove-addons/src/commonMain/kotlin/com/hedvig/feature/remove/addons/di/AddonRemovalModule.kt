package com.hedvig.feature.remove.addons.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.feature.remove.addons.data.GetInsurancesWithRemovableAddonsUseCase
import com.hedvig.feature.remove.addons.data.GetInsurancesWithRemovableAddonsUseCaseImpl
import com.hedvig.feature.remove.addons.data.StartAddonRemovalUseCase
import com.hedvig.feature.remove.addons.data.StartAddonRemovalUseCaseImpl
import com.hedvig.feature.remove.addons.data.SubmitAddonRemovalUseCase
import com.hedvig.feature.remove.addons.data.SubmitAddonRemovalUseCaseImpl
import com.hedvig.feature.remove.addons.ui.RemoveAddonSummaryViewModel
import com.hedvig.feature.remove.addons.ui.SelectAddonToRemoveViewModel
import com.hedvig.feature.remove.addons.ui.SelectInsuranceToRemoveAddonViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val addonRemovalModule = module {
  //includes(addonRemovalPlatformModule)
  viewModel<SelectAddonToRemoveViewModel> { params ->
    SelectAddonToRemoveViewModel(
      get<StartAddonRemovalUseCase>(),
      params.get<Pair<String,String?>>())
  }

  viewModel<RemoveAddonSummaryViewModel> {
    RemoveAddonSummaryViewModel()
  }

  viewModel<SelectInsuranceToRemoveAddonViewModel> {
    SelectInsuranceToRemoveAddonViewModel(
      get<GetInsurancesWithRemovableAddonsUseCase>()
    )
  }

  single<GetInsurancesWithRemovableAddonsUseCase> {
    GetInsurancesWithRemovableAddonsUseCaseImpl(get<ApolloClient>())
  }

  single<StartAddonRemovalUseCase> {
    StartAddonRemovalUseCaseImpl(get<ApolloClient>())
  }

  single<SubmitAddonRemovalUseCase> {
    SubmitAddonRemovalUseCaseImpl(get<ApolloClient>())
  }
}

//expect val addonRemovalPlatformModule: Module
