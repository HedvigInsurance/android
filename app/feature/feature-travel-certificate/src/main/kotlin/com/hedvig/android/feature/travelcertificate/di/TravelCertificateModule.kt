package com.hedvig.android.feature.travelcertificate.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.data.travelcertificate.CheckTravelCertificateAvailabilityForCurrentContractsUseCase
import com.hedvig.android.data.travelcertificate.GetEligibleContractsWithAddressUseCase
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.data.travelcertificate.GetTravelCertificatesHistoryUseCase
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.DownloadTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.ui.choose.ChooseContractForCertificateViewModel
import com.hedvig.android.feature.travelcertificate.ui.generate.GenerateTravelCertificateViewModel
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryViewModel
import com.hedvig.android.feature.travelcertificate.ui.overview.TravelCertificateOverviewViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val travelCertificateModule = module {
  single<CreateTravelCertificateUseCase> { CreateTravelCertificateUseCase(get<ApolloClient>()) }
  single<DownloadTravelCertificateUseCase> { DownloadTravelCertificateUseCase(get()) }
  viewModel<GenerateTravelCertificateViewModel> {
    GenerateTravelCertificateViewModel(
      it.getOrNull<String>(),
      get<GetTravelCertificateSpecificationsUseCase>(),
      get< CreateTravelCertificateUseCase>(),
    )
  }
  viewModel<CertificateHistoryViewModel> {
    CertificateHistoryViewModel(
      get<GetTravelCertificatesHistoryUseCase>(),
      get<DownloadTravelCertificateUseCase>(),
      get<CheckTravelCertificateAvailabilityForCurrentContractsUseCase>(),
      get<GetEligibleContractsWithAddressUseCase>(),
    )
  }
  viewModel<ChooseContractForCertificateViewModel> {
    ChooseContractForCertificateViewModel(
      get<GetEligibleContractsWithAddressUseCase>()
    )
  }
  viewModel<TravelCertificateOverviewViewModel> {
    TravelCertificateOverviewViewModel(
      get<DownloadTravelCertificateUseCase>(),
    )
  }
}
