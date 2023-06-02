package com.hedvig.android.data.travelcertificate.di

import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCase
import org.koin.dsl.module

val travelCertificateDataModule = module {
  single { GetTravelCertificateSpecificationsUseCase(get(octopusClient)) }
}
