package com.hedvig.android.feature.travelcertificate.di

import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificateSpecificationsUseCase
import org.koin.dsl.module

val travelCertificateModule = module {
  single { GetTravelCertificateSpecificationsUseCase(get(octopusClient)) }
}
