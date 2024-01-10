package com.hedvig.android.data.travelcertificate.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCaseImpl
import org.koin.dsl.module

val travelCertificateDataModule = module {
  single<GetTravelCertificateSpecificationsUseCase> {
    GetTravelCertificateSpecificationsUseCaseImpl(get<ApolloClient>())
  }
}
