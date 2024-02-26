package com.hedvig.android.data.travelcertificate.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.data.travelcertificate.CheckTravelCertificateAvailabilityForCurrentContractsUseCase
import com.hedvig.android.data.travelcertificate.CheckTravelCertificateAvailabilityForCurrentContractsUseCaseImpl
import com.hedvig.android.data.travelcertificate.CheckTravelCertificateDestinationAvailabilityUseCase
import com.hedvig.android.data.travelcertificate.CheckTravelCertificateDestinationAvailabilityUseCaseImpl
import com.hedvig.android.data.travelcertificate.GetCoInsuredForContractUseCase
import com.hedvig.android.data.travelcertificate.GetCoInsuredForContractUseCaseImpl
import com.hedvig.android.data.travelcertificate.GetEligibleContractsWithAddressUseCase
import com.hedvig.android.data.travelcertificate.GetEligibleContractsWithAddressUseCaseImpl
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCaseImpl
import com.hedvig.android.data.travelcertificate.GetTravelCertificatesHistoryUseCase
import com.hedvig.android.data.travelcertificate.GetTravelCertificatesHistoryUseCaseImpl
import kotlinx.datetime.Clock
import org.koin.dsl.module

val travelCertificateDataModule = module {
  single<GetTravelCertificateSpecificationsUseCase> {
    GetTravelCertificateSpecificationsUseCaseImpl(get<ApolloClient>())
  }
  single<GetTravelCertificatesHistoryUseCase> {
    GetTravelCertificatesHistoryUseCaseImpl(
      get<ApolloClient>(),
      get<Clock>(),
    )
  }
  single<CheckTravelCertificateDestinationAvailabilityUseCase> {
    CheckTravelCertificateDestinationAvailabilityUseCaseImpl(
      get<GetTravelCertificatesHistoryUseCase>(),
      get<CheckTravelCertificateAvailabilityForCurrentContractsUseCase>(),
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
