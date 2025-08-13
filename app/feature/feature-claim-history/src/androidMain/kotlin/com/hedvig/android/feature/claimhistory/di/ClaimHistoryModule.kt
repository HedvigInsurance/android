package com.hedvig.android.feature.claimhistory.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.feature.claimhistory.ClaimHistoryViewModel
import com.hedvig.android.feature.claimhistory.GetClaimsHistoryUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val claimHistoryModule = module {
  single<GetClaimsHistoryUseCase> {
    GetClaimsHistoryUseCase(apolloClient = get<ApolloClient>())
  }
  viewModel<ClaimHistoryViewModel> {
    ClaimHistoryViewModel(getClaimsHistoryUseCase = get())
  }
}
