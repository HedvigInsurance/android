package com.hedvig.android.feature.travelcertificate.di

import GenerateTravelCertificateViewModel
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val travelCertificateModule = module {
  viewModel { (email: String?, travelCertificateSpecifications: TravelCertificateResult.TravelCertificateSpecifications) ->
    GenerateTravelCertificateViewModel(
      email = email,
      travelCertificateSpecifications = travelCertificateSpecifications,
    )
  }
  single { GetTravelCertificateSpecificationsUseCase(get(octopusClient)) }
}
