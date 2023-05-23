package com.hedvig.android.feature.travelcertificate.di

import GenerateTravelCertificateViewModel
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val travelCertificateModule = module {
  single { GetTravelCertificateSpecificationsUseCase(get(octopusClient)) }
  viewModel { GenerateTravelCertificateViewModel(get()) }
}
