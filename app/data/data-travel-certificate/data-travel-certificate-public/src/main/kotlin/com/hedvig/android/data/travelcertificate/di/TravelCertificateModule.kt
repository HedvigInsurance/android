package com.hedvig.android.data.travelcertificate.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.data.travelcertificate.CheckTravelCertificateAvailabilityUseCase
import com.hedvig.android.data.travelcertificate.CheckTravelCertificateAvailabilityUseCaseImpl
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCaseImpl
import com.hedvig.android.data.travelcertificate.GetTravelCertificatesHistoryUseCase
import com.hedvig.android.data.travelcertificate.GetTravelCertificatesHistoryUseCaseImpl
import org.koin.dsl.module

val travelCertificateDataModule = module {
  single<GetTravelCertificateSpecificationsUseCase> {
    GetTravelCertificateSpecificationsUseCaseImpl(get<ApolloClient>())
  }
  single<GetTravelCertificatesHistoryUseCase> {
    GetTravelCertificatesHistoryUseCaseImpl(get<ApolloClient>())
  }
  single<CheckTravelCertificateAvailabilityUseCase> {
    CheckTravelCertificateAvailabilityUseCaseImpl(
      get<GetTravelCertificatesHistoryUseCase>(),
      get<ApolloClient>(),
    )
  }
}
