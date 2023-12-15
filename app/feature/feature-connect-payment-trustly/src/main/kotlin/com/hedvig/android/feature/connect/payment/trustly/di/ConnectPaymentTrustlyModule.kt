package com.hedvig.android.feature.connect.payment.trustly.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.feature.connect.payment.trustly.StartTrustlySessionUseCase
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.data.TrustlyCallback
import com.hedvig.android.feature.connect.payment.trustly.data.TrustlyCallbackImpl
import com.hedvig.android.market.Market
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val connectPaymentTrustlyModule = module {
  single<TrustlyCallback> { TrustlyCallbackImpl(get<HedvigBuildConstants>()) }
  single<StartTrustlySessionUseCase> {
    StartTrustlySessionUseCase(get<ApolloClient>(), get<TrustlyCallback>())
  }
  viewModel<TrustlyViewModel> { (market: Market) ->
    TrustlyViewModel(
      market = market,
      trustlyCallback = get<TrustlyCallback>(),
      startTrustlySessionUseCase = get<StartTrustlySessionUseCase>(),
      networkCacheManager = get<NetworkCacheManager>(),
    )
  }
}
