package com.hedvig.android.data.claimflow.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import arrow.retrofit.adapter.either.EitherCallAdapterFactory
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.core.appreview.SelfServiceCompletedEventManager
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.data.claimflow.ClaimFlowContextStorage
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowRepositoryImpl
import com.hedvig.android.data.claimflow.OdysseyService
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val claimFlowDataModule = module {
  single<ClaimFlowRepository> {
    ClaimFlowRepositoryImpl(
      get<ApolloClient>(),
      get<OdysseyService>(),
      get<ClaimFlowContextStorage>(),
      get<NetworkCacheManager>(),
      get<SelfServiceCompletedEventManager>(),
      get<CrossSellAfterFlowRepository>(),
    )
  }
  single<ClaimFlowContextStorage> { ClaimFlowContextStorage(get<DataStore<Preferences>>()) }

  single<OdysseyService> {
    val retrofit = Retrofit.Builder()
      .callFactory(get<OkHttpClient>())
      .baseUrl("${get<HedvigBuildConstants>().urlOdyssey}/api/flows/")
      .addCallAdapterFactory(EitherCallAdapterFactory.create())
      .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
      .build()
    retrofit.create(OdysseyService::class.java)
  }
}
