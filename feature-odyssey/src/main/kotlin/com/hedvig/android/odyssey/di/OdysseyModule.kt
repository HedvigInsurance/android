package com.hedvig.android.odyssey.di

import arrow.retrofit.adapter.either.EitherCallAdapterFactory
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.odyssey.data.ClaimFlowRepository
import com.hedvig.android.odyssey.data.ClaimFlowRepositoryImpl
import com.hedvig.android.odyssey.data.OdysseyService
import com.hedvig.android.odyssey.model.FlowId
import com.hedvig.android.odyssey.navigation.ClaimFlowDestination
import com.hedvig.android.odyssey.navigation.LocationOption
import com.hedvig.android.odyssey.sdui.AndroidDatadogLogger
import com.hedvig.android.odyssey.sdui.AndroidDatadogProvider
import com.hedvig.android.odyssey.search.GetNetworkClaimEntryPointsUseCase
import com.hedvig.android.odyssey.search.SearchViewModel
import com.hedvig.android.odyssey.step.audiorecording.AudioRecordingViewModel
import com.hedvig.android.odyssey.step.dateofoccurrence.DateOfOccurrenceViewModel
import com.hedvig.android.odyssey.step.dateofoccurrencepluslocation.DateOfOccurrencePlusLocationViewModel
import com.hedvig.android.odyssey.step.location.LocationViewModel
import com.hedvig.android.odyssey.step.phonenumber.PhoneNumberViewModel
import com.hedvig.android.odyssey.step.singleitem.SingleItemViewModel
import com.hedvig.android.odyssey.step.singleitemcheckout.SingleItemCheckoutViewModel
import com.hedvig.android.odyssey.step.start.ClaimFlowStartStepViewModel
import com.hedvig.android.odyssey.step.summary.ClaimSummaryViewModel
import com.hedvig.odyssey.datadog.DatadogLogger
import com.hedvig.odyssey.datadog.DatadogProvider
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * The URL targeting odyssey backend
 */
val odysseyUrlQualifier = qualifier("odysseyUrlQualifier")

@Suppress("RemoveExplicitTypeArguments")
val odysseyModule = module {
  single<DatadogLogger> { AndroidDatadogLogger() }
  single<DatadogProvider> { AndroidDatadogProvider(get(), get()) }

  single<ClaimFlowRepository> {
    ClaimFlowRepositoryImpl(get<ApolloClient>(octopusClient), get<OdysseyService>())
  }

  viewModel<SearchViewModel> { SearchViewModel(get<GetNetworkClaimEntryPointsUseCase>()) }
  single<GetNetworkClaimEntryPointsUseCase> {
    GetNetworkClaimEntryPointsUseCase(get<ApolloClient>(octopusClient))
  }

  // Claims
  viewModel<ClaimFlowStartStepViewModel> { (entryPointId: String?) ->
    ClaimFlowStartStepViewModel(entryPointId, get())
  }
  viewModel<AudioRecordingViewModel> { (flowId: FlowId) -> AudioRecordingViewModel(flowId, get()) }
  viewModel<PhoneNumberViewModel> { (initialPhoneNumber: String?) ->
    PhoneNumberViewModel(initialPhoneNumber, get())
  }
  viewModel<DateOfOccurrenceViewModel> { (initialDateOfOccurrence: LocalDate?, maxDate: LocalDate) ->
    DateOfOccurrenceViewModel(
      initialDateOfOccurrence = initialDateOfOccurrence,
      maxDate = maxDate,
      claimFlowRepository = get(),
    )
  }
  viewModel<LocationViewModel> { (selectedLocation: String?, locationOptions: List<LocationOption>) ->
    LocationViewModel(selectedLocation, locationOptions, get())
  }
  viewModel<DateOfOccurrencePlusLocationViewModel> { parametersHolder ->
    val dateOfOccurrence: LocalDate? = parametersHolder.getOrNull()
    val maxDate: LocalDate = parametersHolder.get()
    val selectedLocation: String? = parametersHolder.getOrNull()
    val locationOptions: List<LocationOption> = parametersHolder.get()
    DateOfOccurrencePlusLocationViewModel(
      initialDateOfOccurrence = dateOfOccurrence,
      maxDate = maxDate,
      selectedLocation = selectedLocation,
      locationOptions = locationOptions,
      get<ClaimFlowRepository>(),
    )
  }
  viewModel<SingleItemViewModel> { (singleItem: ClaimFlowDestination.SingleItem) ->
    SingleItemViewModel(singleItem, get<ClaimFlowRepository>())
  }
  viewModel<ClaimSummaryViewModel> { (summary: ClaimFlowDestination.Summary) ->
    ClaimSummaryViewModel(summary, get<ClaimFlowRepository>())
  }
  viewModel<SingleItemCheckoutViewModel> { (singleItemCheckout: ClaimFlowDestination.SingleItemCheckout) ->
    SingleItemCheckoutViewModel(singleItemCheckout, get<ClaimFlowRepository>())
  }

  // Retrofit
  single<Retrofit> {
    Retrofit.Builder()
      .callFactory(get<OkHttpClient>())
      .baseUrl("${get<String>(odysseyUrlQualifier)}/api/flows/")
      .addCallAdapterFactory(EitherCallAdapterFactory.create())
      .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
      .build()
  }
  single<OdysseyService> {
    get<Retrofit>().create(OdysseyService::class.java)
  }
}
