package com.hedvig.android.data.changetier.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.changetier.data.ChangeTierQuoteStorage
import com.hedvig.android.data.changetier.data.ChangeTierQuoteStorageImpl
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.ChangeTierRepositoryImpl
import com.hedvig.android.data.changetier.data.CreateChangeTierDeductibleIntentUseCase
import com.hedvig.android.data.changetier.data.CreateChangeTierDeductibleIntentUseCaseImpl
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.featureflags.FeatureManager
import org.koin.dsl.module

val dataChangeTierModule = module {
  single<CreateChangeTierDeductibleIntentUseCase> {
    CreateChangeTierDeductibleIntentUseCaseImpl(
      get<ApolloClient>(),
      get<FeatureManager>(),
    )
  }
  single<ChangeTierQuoteStorage> {
    ChangeTierQuoteStorageImpl(get<DataStore<Preferences>>())
  }
  single<ChangeTierRepository> {
    ChangeTierRepositoryImpl(
      createChangeTierDeductibleIntentUseCase = get<CreateChangeTierDeductibleIntentUseCase>(),
      changeTierQuoteStorage = get<ChangeTierQuoteStorage>(),
      crossSellAfterFlowRepository = get<CrossSellAfterFlowRepository>(),
      apolloClient = get<ApolloClient>(),
    )
  }
}
