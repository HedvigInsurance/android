package com.hedvig.android.data.claimtriaging.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.data.claimtriaging.GetEntryPointGroupsUseCase
import com.hedvig.android.data.claimtriaging.GetEntryPointsUseCase
import org.koin.dsl.module

val claimTriagingDataModule = module {
  single<GetEntryPointsUseCase> { GetEntryPointsUseCase(get<ApolloClient>(octopusClient)) }
  single<GetEntryPointGroupsUseCase> { GetEntryPointGroupsUseCase(get<ApolloClient>(octopusClient)) }
}
