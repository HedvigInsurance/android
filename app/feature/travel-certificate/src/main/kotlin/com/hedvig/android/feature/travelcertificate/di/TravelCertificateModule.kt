package com.hedvig.android.feature.travelcertificate.di

import GenerateTravelCertificateViewModel
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.DownloadTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificateSpecificationsUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val travelCertificateModule = module {
  single { GetTravelCertificateSpecificationsUseCase(get(octopusClient)) }
  single { CreateTravelCertificateUseCase(get(octopusClient)) }
  single { DownloadTravelCertificateUseCase(get()) }

  viewModel { GenerateTravelCertificateViewModel(get(), get(), get()) }
}
