package com.hedvig.android.feature.travelcertificate.di

import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.feature.travelcertificate.GenerateTravelCertificateViewModel
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.DownloadTravelCertificateUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val travelCertificateModule = module {
  single<CreateTravelCertificateUseCase> { CreateTravelCertificateUseCase(get(octopusClient)) }
  single<DownloadTravelCertificateUseCase> { DownloadTravelCertificateUseCase(get()) }
  viewModel<GenerateTravelCertificateViewModel> { GenerateTravelCertificateViewModel(get(), get(), get()) }
}
