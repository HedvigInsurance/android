package com.hedvig.android.feature.travelcertificate.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.data.travelcertificate.CheckTravelCertificateAvailabilityForCurrentContractsUseCase
import com.hedvig.android.data.travelcertificate.GetCoInsuredForContractUseCase
import com.hedvig.android.data.travelcertificate.GetEligibleContractsWithAddressUseCase
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.data.travelcertificate.GetTravelCertificatesHistoryUseCase
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateDestination
import com.hedvig.android.feature.travelcertificate.ui.choose.ChooseContractForCertificateViewModel
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputViewModel
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputViewModel
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryViewModel
import com.hedvig.android.feature.travelcertificate.ui.overview.TravelCertificateOverviewViewModel
import com.hedvig.android.language.LanguageService
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val travelCertificateModule = module {
  single<CreateTravelCertificateUseCase> { CreateTravelCertificateUseCase(get<ApolloClient>()) }
  viewModel<CertificateHistoryViewModel> {
    CertificateHistoryViewModel(
      get<GetTravelCertificatesHistoryUseCase>(),
      get<DownloadPdfUseCase>(),
      get<CheckTravelCertificateAvailabilityForCurrentContractsUseCase>(),
      get<GetEligibleContractsWithAddressUseCase>(),
    )
  }
  viewModel<ChooseContractForCertificateViewModel> {
    ChooseContractForCertificateViewModel(
      get<GetEligibleContractsWithAddressUseCase>(),
    )
  }
  viewModel<TravelCertificateOverviewViewModel> {
    TravelCertificateOverviewViewModel(
      get<DownloadPdfUseCase>(),
    )
  }
  viewModel<TravelCertificateDateInputViewModel> {
    TravelCertificateDateInputViewModel(
      it.getOrNull<String>(),
      get<GetTravelCertificateSpecificationsUseCase>(),
      get<CreateTravelCertificateUseCase>(),
      get<LanguageService>(),
    )
  }
  viewModel<TravelCertificateTravellersInputViewModel> {
    TravelCertificateTravellersInputViewModel(
      it.get<TravelCertificateDestination.TravelCertificateTravellersInput.TravelCertificatePrimaryInput>(),
      get<CreateTravelCertificateUseCase>(),
      get<GetCoInsuredForContractUseCase>(),
    )
  }
}
