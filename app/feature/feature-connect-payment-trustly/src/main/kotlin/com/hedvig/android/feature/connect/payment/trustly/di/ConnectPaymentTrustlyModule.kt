package com.hedvig.android.feature.connect.payment.trustly.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.feature.connect.payment.trustly.StartTrustlySessionUseCase
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.data.TrustlyCallback
import com.hedvig.android.feature.connect.payment.trustly.data.TrustlyCallbackImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val connectPaymentTrustlyModule = module {
  single<TrustlyCallback> { TrustlyCallbackImpl(get<HedvigBuildConstants>()) }
  single<StartTrustlySessionUseCase> {
    StartTrustlySessionUseCase(get<ApolloClient>(octopusClient), get<TrustlyCallback>())
  }
  viewModel<TrustlyViewModel> { TrustlyViewModel(get<TrustlyCallback>(), get<StartTrustlySessionUseCase>()) }
}
