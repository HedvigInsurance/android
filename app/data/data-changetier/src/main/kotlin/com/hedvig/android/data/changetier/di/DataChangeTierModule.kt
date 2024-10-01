package com.hedvig.android.data.changetier.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.changetier.data.CreateChangeTierDeductibleIntentUseCase
import com.hedvig.android.data.changetier.data.CreateChangeTierDeductibleIntentUseCaseImpl
import com.hedvig.android.featureflags.FeatureManager
import org.koin.dsl.module

val dataChangeTierModule = module {
  single<CreateChangeTierDeductibleIntentUseCase> {
    CreateChangeTierDeductibleIntentUseCaseImpl(
      get<ApolloClient>(),
      get<FeatureManager>(),
    )
  }
}
