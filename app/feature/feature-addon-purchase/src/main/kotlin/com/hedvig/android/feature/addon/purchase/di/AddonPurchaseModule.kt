package com.hedvig.android.feature.addon.purchase.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.feature.addon.purchase.data.GetInsuranceForTravelAddonUseCase
import com.hedvig.android.feature.addon.purchase.data.GetInsuranceForTravelAddonUseCaseImpl
import com.hedvig.android.featureflags.FeatureManager
import org.koin.dsl.module

val addonPurchaseModule = module {
//  viewModel<SelectCoverageViewModel> { params ->
//    SelectCoverageViewModel(
//      params = params.get<InsuranceCustomizationParameters>(),
//      tierRepository = get<ChangeTierRepository>(),
//      getCurrentContractDataUseCase = get<GetCurrentContractDataUseCase>(),
//    )
//  }
//
  single<GetInsuranceForTravelAddonUseCase> {
    GetInsuranceForTravelAddonUseCaseImpl(
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
    )
  }
}
