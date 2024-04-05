package com.hedvig.android.feature.help.center.di

import com.hedvig.android.data.termination.data.GetTerminatableContractsUseCase
import com.hedvig.android.data.travelcertificate.CheckTravelCertificateDestinationAvailabilityUseCase
import com.hedvig.android.feature.help.center.data.GetCommonClaimsUseCase
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import org.koin.dsl.module

val helpCenterDataModule = module {
  single<GetCommonClaimsUseCase> {
    GetCommonClaimsUseCase(get())
  }
  single<GetQuickLinksUseCase> {
    GetQuickLinksUseCase(
      apolloClient = get(),
      featureManager = get(),
      get<CheckTravelCertificateDestinationAvailabilityUseCase>(),
      get<GetTerminatableContractsUseCase>(),
    )
  }
}
