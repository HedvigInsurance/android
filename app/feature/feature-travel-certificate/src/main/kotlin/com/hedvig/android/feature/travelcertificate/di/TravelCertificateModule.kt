package com.hedvig.android.feature.travelcertificate.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.feature.travelcertificate.data.CheckTravelCertificateAvailabilityForCurrentContractsUseCase
import com.hedvig.android.feature.travelcertificate.data.CheckTravelCertificateAvailabilityForCurrentContractsUseCaseImpl
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.GetCoInsuredForContractUseCase
import com.hedvig.android.feature.travelcertificate.data.GetCoInsuredForContractUseCaseImpl
import com.hedvig.android.feature.travelcertificate.data.GetEligibleContractsWithAddressUseCase
import com.hedvig.android.feature.travelcertificate.data.GetEligibleContractsWithAddressUseCaseImpl
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificateSpecificationsUseCaseImpl
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificatesHistoryUseCase
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificatesHistoryUseCaseImpl
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateDestination
import com.hedvig.android.feature.travelcertificate.ui.choose.ChooseContractForCertificateViewModel
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputViewModel
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputViewModel
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryViewModel
import com.hedvig.android.feature.travelcertificate.ui.overview.TravelCertificateOverviewViewModel
import com.hedvig.android.language.LanguageService
import kotlinx.datetime.Clock
import org.koin.core.module.dsl.viewModel
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
  single<GetTravelCertificateSpecificationsUseCase> {
    GetTravelCertificateSpecificationsUseCaseImpl(get<ApolloClient>())
  }
  single<GetTravelCertificatesHistoryUseCase> {
    GetTravelCertificatesHistoryUseCaseImpl(
      get<ApolloClient>(),
      get<Clock>(),
    )
  }
  single<CheckTravelCertificateAvailabilityForCurrentContractsUseCase> {
    CheckTravelCertificateAvailabilityForCurrentContractsUseCaseImpl(get<ApolloClient>())
  }
  single<GetEligibleContractsWithAddressUseCase> {
    GetEligibleContractsWithAddressUseCaseImpl(get<ApolloClient>())
  }
  single<GetCoInsuredForContractUseCase> {
    GetCoInsuredForContractUseCaseImpl(get<ApolloClient>())
  }
}
