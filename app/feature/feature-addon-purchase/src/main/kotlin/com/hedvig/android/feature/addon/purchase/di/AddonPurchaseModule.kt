package com.hedvig.android.feature.addon.purchase.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.addons.data.GetAddonBannerInfoUseCase
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.feature.addon.purchase.data.GetAddonOfferUseCase
import com.hedvig.android.feature.addon.purchase.data.GetAddonOfferUseCaseImpl
import com.hedvig.android.feature.addon.purchase.data.GetInsuranceForTravelAddonUseCase
import com.hedvig.android.feature.addon.purchase.data.GetInsuranceForTravelAddonUseCaseImpl
import com.hedvig.android.feature.addon.purchase.data.GetQuoteCostBreakdownUseCase
import com.hedvig.android.feature.addon.purchase.data.GetQuoteCostBreakdownUseCaseImpl
import com.hedvig.android.feature.addon.purchase.data.SubmitAddonPurchaseUseCase
import com.hedvig.android.feature.addon.purchase.data.SubmitAddonPurchaseUseCaseImpl
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeAddonViewModel
import com.hedvig.android.feature.addon.purchase.ui.selectinsurance.SelectInsuranceForAddonViewModel
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryViewModel
import com.hedvig.android.feature.addon.purchase.ui.triage.TravelAddonTriageViewModel
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

  viewModel<CustomizeAddonViewModel> { params ->
    CustomizeAddonViewModel(
      insuranceId = params.get<String>(),
      preselectedAddonDisplayNames = params.get<List<String>>(),
      getAddonOfferUseCase = get<GetAddonOfferUseCase>(),
    )
  }

  viewModel<AddonSummaryViewModel> { params ->
    AddonSummaryViewModel(
      summaryParameters = params.get<SummaryParameters>(),
      addonPurchaseSource = params.get<AddonBannerSource>(),
      submitAddonPurchaseUseCase = get<SubmitAddonPurchaseUseCase>(),
      getQuoteCostBreakdownUseCase = get<GetQuoteCostBreakdownUseCase>(),
      getInsuranceForTravelAddonUseCase = get<GetInsuranceForTravelAddonUseCase>(),
    )
  }

  viewModel<TravelAddonTriageViewModel> {
    TravelAddonTriageViewModel(
      get<GetAddonBannerInfoUseCase>(),
    )
  }

  single<GetQuoteCostBreakdownUseCase> {
    GetQuoteCostBreakdownUseCaseImpl(
      apolloClient = get<ApolloClient>(),
    )
  }

  single<GetInsuranceForTravelAddonUseCase> {
    GetInsuranceForTravelAddonUseCaseImpl(
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
    )
  }

  single<GetAddonOfferUseCase> {
    GetAddonOfferUseCaseImpl(
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
    )
  }

  single<SubmitAddonPurchaseUseCase> {
    SubmitAddonPurchaseUseCaseImpl(
      apolloClient = get<ApolloClient>(),
      crossSellAfterFlowRepository = get<CrossSellAfterFlowRepository>(),
    )
  }
}
