package com.hedvig.android.data.changetier.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.ChangeTierRepositoryImpl
import com.hedvig.android.data.changetier.data.CreateChangeTierDeductibleIntentUseCase
import com.hedvig.android.data.changetier.data.CreateChangeTierDeductibleIntentUseCaseImpl
import com.hedvig.android.data.changetier.database.TierQuoteMapper
import com.hedvig.android.data.chat.database.TierQuoteDao
import com.hedvig.android.featureflags.FeatureManager
import org.koin.dsl.module

val dataChangeTierModule = module {
  single<CreateChangeTierDeductibleIntentUseCase> {
    CreateChangeTierDeductibleIntentUseCaseImpl(
      get<ApolloClient>(),
      get<FeatureManager>(),
    )
  }
  single<TierQuoteMapper> {
    TierQuoteMapper()
  }
  single<ChangeTierRepository> {
    ChangeTierRepositoryImpl(
      createChangeTierDeductibleIntentUseCase = get<CreateChangeTierDeductibleIntentUseCase>(),
      tierQuoteDao = get<TierQuoteDao>(),
      mapper = get<TierQuoteMapper>(),
    )
  }
}
