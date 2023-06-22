package com.hedvig.android.feature.odyssey.di

import arrow.retrofit.adapter.either.EitherCallAdapterFactory
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.feature.odyssey.data.ClaimFlowRepository
import com.hedvig.android.feature.odyssey.data.ClaimFlowRepositoryImpl
import com.hedvig.android.feature.odyssey.data.OdysseyService
import com.hedvig.android.feature.odyssey.model.FlowId
import com.hedvig.android.feature.odyssey.navigation.AudioContent
import com.hedvig.android.feature.odyssey.navigation.ClaimFlowDestination
import com.hedvig.android.feature.odyssey.navigation.LocationOption
import com.hedvig.android.feature.odyssey.step.audiorecording.AudioRecordingViewModel
import com.hedvig.android.feature.odyssey.step.dateofoccurrence.DateOfOccurrenceViewModel
import com.hedvig.android.feature.odyssey.step.dateofoccurrencepluslocation.DateOfOccurrencePlusLocationViewModel
import com.hedvig.android.feature.odyssey.step.honestypledge.HonestyPledgeViewModel
import com.hedvig.android.feature.odyssey.step.location.LocationViewModel
import com.hedvig.android.feature.odyssey.step.notificationpermission.NotificationPermissionViewModel
import com.hedvig.android.feature.odyssey.step.phonenumber.PhoneNumberViewModel
import com.hedvig.android.feature.odyssey.step.singleitem.SingleItemViewModel
import com.hedvig.android.feature.odyssey.step.singleitemcheckout.SingleItemCheckoutViewModel
import com.hedvig.android.feature.odyssey.step.summary.ClaimSummaryViewModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
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
  single<ClaimFlowRepository> {
    ClaimFlowRepositoryImpl(get<ApolloClient>(octopusClient), get<OdysseyService>())
  }

  // Claims
  viewModel<HonestyPledgeViewModel> { (entryPointId: EntryPointId?) ->
    HonestyPledgeViewModel(entryPointId, get())
  }
  viewModel<NotificationPermissionViewModel> { (entryPointId: EntryPointId?) ->
    NotificationPermissionViewModel(entryPointId, get<ClaimFlowRepository>())
  }
  viewModel<AudioRecordingViewModel> { (flowId: FlowId, audioContent: AudioContent?) ->
    AudioRecordingViewModel(
      flowId = flowId,
      audioContent = audioContent,
      claimFlowRepository = get(),
    )
  }
  viewModel<PhoneNumberViewModel> { (initialPhoneNumber: String?) ->
    PhoneNumberViewModel(initialPhoneNumber, get())
  }
  viewModel<DateOfOccurrenceViewModel> { (dateOfOccurrence: ClaimFlowDestination.DateOfOccurrence) ->
    DateOfOccurrenceViewModel(
      dateOfOccurrence = dateOfOccurrence,
      claimFlowRepository = get(),
    )
  }
  viewModel<LocationViewModel> { (selectedLocation: String?, locationOptions: List<LocationOption>) ->
    LocationViewModel(selectedLocation, locationOptions, get())
  }
  viewModel<DateOfOccurrencePlusLocationViewModel> { parametersHolder ->
    val dateOfOccurrencePlusLocation: ClaimFlowDestination.DateOfOccurrencePlusLocation = parametersHolder.get()
    DateOfOccurrencePlusLocationViewModel(
      dateOfOccurrencePlusLocation = dateOfOccurrencePlusLocation,
      claimFlowRepository = get<ClaimFlowRepository>(),
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
