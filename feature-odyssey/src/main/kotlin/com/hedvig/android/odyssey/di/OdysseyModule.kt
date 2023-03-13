package com.hedvig.android.odyssey.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.odyssey.sdui.AndroidDatadogLogger
import com.hedvig.android.odyssey.sdui.AndroidDatadogProvider
import com.hedvig.android.odyssey.input.InputViewModel
import com.hedvig.android.odyssey.input.ui.audiorecorder.AudioRecorderViewModel
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.android.odyssey.repository.ClaimsFlowRepository
import com.hedvig.android.odyssey.repository.NetworkClaimsFlowRepository
import com.hedvig.android.odyssey.repository.PhoneNumberRepository
import com.hedvig.android.odyssey.resolution.ResolutionViewModel
import com.hedvig.android.odyssey.search.GetNetworkClaimEntryPointsUseCase
import com.hedvig.android.odyssey.search.SearchViewModel
import com.hedvig.odyssey.datadog.DatadogLogger
import com.hedvig.odyssey.datadog.DatadogProvider
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

/**
 * The URL targeting odyssey backend
 */
val odysseyUrlQualifier = qualifier("odysseyUrlQualifier")

@Suppress("RemoveExplicitTypeArguments")
val odysseyModule = module {
  single<DatadogLogger> { AndroidDatadogLogger() }
  single<DatadogProvider> { AndroidDatadogProvider(get(), get()) }

  viewModel<AudioRecorderViewModel> { AudioRecorderViewModel(get()) }

  viewModel<ResolutionViewModel> { (resolution: Resolution) ->
    ResolutionViewModel(get(), resolution)
  }
  viewModel<InputViewModel> { (commonClaimId: String) ->
    InputViewModel(
      commonClaimId = commonClaimId,
      repository = get<ClaimsFlowRepository>(),
      getPhoneNumberUseCase = get<PhoneNumberRepository>(),
    )
  }
  single<PhoneNumberRepository> {
    PhoneNumberRepository(get<ApolloClient>(giraffeClient))
  }
  single<ClaimsFlowRepository> {
    NetworkClaimsFlowRepository(get<OkHttpClient>())
  }

  viewModel<SearchViewModel> { SearchViewModel(get<GetNetworkClaimEntryPointsUseCase>()) }
  single<GetNetworkClaimEntryPointsUseCase> {
    GetNetworkClaimEntryPointsUseCase(
      get(),
      get(odysseyUrlQualifier),
    )
  }
}
