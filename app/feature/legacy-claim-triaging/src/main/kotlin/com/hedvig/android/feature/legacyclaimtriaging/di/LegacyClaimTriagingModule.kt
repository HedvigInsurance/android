package com.hedvig.android.feature.legacyclaimtriaging.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.feature.legacyclaimtriaging.GetEntryPointsUseCase
import com.hedvig.android.feature.legacyclaimtriaging.LegacyClaimTriagingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val legacyClaimTriagingModule = module {
  viewModel<LegacyClaimTriagingViewModel> { LegacyClaimTriagingViewModel(get()) }
  single<GetEntryPointsUseCase> { GetEntryPointsUseCase(get<ApolloClient>(octopusClient)) }
}
