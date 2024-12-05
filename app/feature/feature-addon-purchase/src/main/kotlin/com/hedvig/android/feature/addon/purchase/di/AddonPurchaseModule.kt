package com.hedvig.android.feature.addon.purchase.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.feature.addon.purchase.data.GetInsuranceForTravelAddonUseCase
import com.hedvig.android.feature.addon.purchase.data.GetInsuranceForTravelAddonUseCaseImpl
import com.hedvig.android.feature.addon.purchase.data.GetTravelAddonOfferUseCase
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonViewModel
import com.hedvig.android.feature.addon.purchase.ui.selectinsurance.SelectInsuranceForAddonViewModel
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryViewModel
import com.hedvig.android.featureflags.FeatureManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val addonPurchaseModule = module {
  viewModel<SelectInsuranceForAddonViewModel> { params ->
    SelectInsuranceForAddonViewModel(
      ids = params.get<List<String>>(),
      getInsuranceForTravelAddonUseCase = get<GetInsuranceForTravelAddonUseCase>(),
    )
  }

  viewModel<CustomizeTravelAddonViewModel> { params ->
    CustomizeTravelAddonViewModel(
      insuranceId = params.get<String>(),
      getTravelAddonOfferUseCase = get<GetTravelAddonOfferUseCase>(),
    )
  }

  viewModel<AddonSummaryViewModel> { params ->
    AddonSummaryViewModel(
      summaryParameters = params.get<SummaryParameters>(),
    )
  }

  single<GetInsuranceForTravelAddonUseCase> {
    GetInsuranceForTravelAddonUseCaseImpl(
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
    )
  }

  // todo: add use case impl
  single<GetTravelAddonOfferUseCase> {
    TODO()
//    GetTravelAddonOfferUseCaseImpl(
//      apolloClient = get<ApolloClient>(),
//      featureManager = get<FeatureManager>(),
//    )
  }
}
