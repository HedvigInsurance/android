package com.hedvig.android.feature.travelcertificate.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.data.travelcertificate.GetTravelCertificatesHistoryUseCase
import com.hedvig.android.feature.travelcertificate.CertificateHistoryViewModel
import com.hedvig.android.feature.travelcertificate.GenerateTravelCertificateViewModel
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.DownloadTravelCertificateUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val travelCertificateModule = module {
  single<CreateTravelCertificateUseCase> { CreateTravelCertificateUseCase(get<ApolloClient>()) }
  single<DownloadTravelCertificateUseCase> { DownloadTravelCertificateUseCase(get()) }
  viewModel<GenerateTravelCertificateViewModel> { GenerateTravelCertificateViewModel(get(), get(), get()) }
  viewModel<CertificateHistoryViewModel> {
    CertificateHistoryViewModel(
      get<GetTravelCertificatesHistoryUseCase>(),
      get<DownloadTravelCertificateUseCase>(),
    )
  }
}
