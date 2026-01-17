package com.hedvig.android.data.claimflow.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.core.appreview.SelfServiceCompletedEventManager
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.data.claimflow.ClaimFlowContextStorage
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowRepositoryImpl
import com.hedvig.android.data.claimflow.OdysseyService
import io.ktor.client.HttpClient
import org.koin.dsl.module

val claimFlowDataModule = module {
  single<ClaimFlowRepository> {
    ClaimFlowRepositoryImpl(
      get<ApolloClient>(),
      get<OdysseyService>(),
      get<ClaimFlowContextStorage>(),
      get<NetworkCacheManager>(),
      get<SelfServiceCompletedEventManager>(),
    )
  }
  single<ClaimFlowContextStorage> { ClaimFlowContextStorage(get<DataStore<Preferences>>()) }

  single<OdysseyService> {
    OdysseyService(
      httpClient = get<HttpClient>(),
      hedvigBuildConstants = get<HedvigBuildConstants>(),
    )
  }
}
