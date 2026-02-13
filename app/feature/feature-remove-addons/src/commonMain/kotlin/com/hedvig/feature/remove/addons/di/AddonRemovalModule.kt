package com.hedvig.feature.remove.addons.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.feature.remove.addons.data.GetAddonRemovalCostBreakdownUseCase
import com.hedvig.feature.remove.addons.data.GetAddonRemovalCostBreakdownUseCaseImpl
import com.hedvig.feature.remove.addons.data.GetInsurancesWithRemovableAddonsUseCase
import com.hedvig.feature.remove.addons.data.GetInsurancesWithRemovableAddonsUseCaseImpl
import com.hedvig.feature.remove.addons.data.StartAddonRemovalUseCase
import com.hedvig.feature.remove.addons.data.StartAddonRemovalUseCaseImpl
import com.hedvig.feature.remove.addons.data.SubmitAddonRemovalUseCase
import com.hedvig.feature.remove.addons.data.SubmitAddonRemovalUseCaseImpl
import com.hedvig.feature.remove.addons.ui.CommonSummaryParameters
import com.hedvig.feature.remove.addons.ui.RemoveAddonSummaryViewModel
import com.hedvig.feature.remove.addons.ui.SelectAddonToRemoveViewModel
import com.hedvig.feature.remove.addons.ui.SelectInsuranceToRemoveAddonViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val addonRemovalModule = module {
  viewModel<SelectAddonToRemoveViewModel> { params ->
    SelectAddonToRemoveViewModel(
      get<StartAddonRemovalUseCase>(),
      params.get<Pair<String,String?>>())
  }

  viewModel<RemoveAddonSummaryViewModel> {params ->
    RemoveAddonSummaryViewModel(
      params = params.get<CommonSummaryParameters>(),
      submitAddonRemovalUseCase = get<SubmitAddonRemovalUseCase>(),
      getAddonRemovalCostBreakdownUseCase = get<GetAddonRemovalCostBreakdownUseCase>()
    )
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

  single<GetAddonRemovalCostBreakdownUseCase> {
    GetAddonRemovalCostBreakdownUseCaseImpl(get<ApolloClient>())
  }
}
