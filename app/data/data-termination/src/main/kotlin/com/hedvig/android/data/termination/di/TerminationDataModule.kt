package com.hedvig.android.data.termination.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.data.termination.data.GetTerminatableContractsUseCase
import com.hedvig.android.data.termination.data.GetTerminatableContractsUseCaseImpl
import org.koin.dsl.module

val terminationDataModule = module {
  single<GetTerminatableContractsUseCase> {
    GetTerminatableContractsUseCaseImpl(get<ApolloClient>())
  }
}
