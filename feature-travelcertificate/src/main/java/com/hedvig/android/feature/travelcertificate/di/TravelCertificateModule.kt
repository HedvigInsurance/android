package com.hedvig.android.feature.travelcertificate.di

import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificateUseCase
import com.hedvig.android.apollo.octopus.di.octopusClient
import org.koin.dsl.module

val travelCertificateModule = module {
  single { GetTravelCertificateUseCase(get(octopusClient)) }
}
