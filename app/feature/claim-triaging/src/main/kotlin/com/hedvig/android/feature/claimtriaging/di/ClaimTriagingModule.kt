package com.hedvig.android.feature.claimtriaging.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.data.claimtriaging.EntryPoint
import com.hedvig.android.data.claimtriaging.EntryPointOption
import com.hedvig.android.feature.claimtriaging.GetEntryPointGroupsUseCase
import com.hedvig.android.feature.claimtriaging.claimentrypointoptions.ClaimEntryPointOptionsViewModel
import com.hedvig.android.feature.claimtriaging.claimentrypoints.ClaimEntryPointsViewModel
import com.hedvig.android.feature.claimtriaging.claimgroups.ClaimGroupsViewModel
import kotlinx.collections.immutable.ImmutableList
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val claimTriagingModule = module {
  viewModel<ClaimGroupsViewModel> { ClaimGroupsViewModel(get()) }
  viewModel<ClaimEntryPointsViewModel> { (entryPoints: ImmutableList<EntryPoint>) ->
    ClaimEntryPointsViewModel(entryPoints)
  }
  viewModel<ClaimEntryPointOptionsViewModel> { (entryPointOptions: ImmutableList<EntryPointOption>) ->
    ClaimEntryPointOptionsViewModel(entryPointOptions)
  }
  single<GetEntryPointGroupsUseCase> { GetEntryPointGroupsUseCase(get<ApolloClient>(octopusClient)) }
}
