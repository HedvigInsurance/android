package com.hedvig.android.feature.claimtriaging.di

import androidx.lifecycle.SavedStateHandle
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.feature.claimtriaging.GetEntryPointGroupsUseCase
import com.hedvig.android.feature.claimtriaging.claimentrypointoptions.ClaimEntryPointOptionsViewModel
import com.hedvig.android.feature.claimtriaging.claimentrypoints.ClaimEntryPointsViewModel
import com.hedvig.android.feature.claimtriaging.claimgroups.ClaimGroupsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val claimTriagingModule = module {
  viewModel<ClaimGroupsViewModel> {
    ClaimGroupsViewModel(get<GetEntryPointGroupsUseCase>(), get<ClaimFlowRepository>())
  }
  viewModel<ClaimEntryPointsViewModel> {
    ClaimEntryPointsViewModel(get<SavedStateHandle>(), get<ClaimFlowRepository>())
  }
  viewModel<ClaimEntryPointOptionsViewModel> {
    ClaimEntryPointOptionsViewModel(get<SavedStateHandle>(), get<ClaimFlowRepository>())
  }
  single<GetEntryPointGroupsUseCase> { GetEntryPointGroupsUseCase(get<ApolloClient>()) }
}
