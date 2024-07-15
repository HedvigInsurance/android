package com.hedvig.android.feature.claimtriaging.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimtriaging.EntryPoint
import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.data.claimtriaging.EntryPointOption
import com.hedvig.android.feature.claimtriaging.GetEntryPointGroupsUseCase
import com.hedvig.android.feature.claimtriaging.claimentrypointoptions.ClaimEntryPointOptionsViewModel
import com.hedvig.android.feature.claimtriaging.claimentrypoints.ClaimEntryPointsViewModel
import com.hedvig.android.feature.claimtriaging.claimgroups.ClaimGroupsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.ParametersHolder
import org.koin.dsl.module

val claimTriagingModule = module {
  viewModel<ClaimGroupsViewModel> {
    ClaimGroupsViewModel(get<GetEntryPointGroupsUseCase>(), get<ClaimFlowRepository>())
  }
  viewModel<ClaimEntryPointsViewModel> { (entryPoints: List<EntryPoint>) ->
    ClaimEntryPointsViewModel(entryPoints, get())
  }
  viewModel<ClaimEntryPointOptionsViewModel> { parametersHolder: ParametersHolder ->
    val entryPointId: EntryPointId = parametersHolder.get()
    val entryPointOptions: List<EntryPointOption> = parametersHolder.get()
    ClaimEntryPointOptionsViewModel(entryPointId, entryPointOptions, get<ClaimFlowRepository>())
  }
  single<GetEntryPointGroupsUseCase> { GetEntryPointGroupsUseCase(get<ApolloClient>()) }
}
