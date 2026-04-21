package com.hedvig.android.data.termination.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.termination.data.GetTerminatableContractsUseCase
import com.hedvig.android.data.termination.data.GetTerminatableContractsUseCaseImpl
import com.hedvig.android.featureflags.FeatureManager
import org.koin.dsl.module

val terminationDataModule = module {
  single<GetTerminatableContractsUseCase> {
    GetTerminatableContractsUseCaseImpl(
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
    )
  }
}
