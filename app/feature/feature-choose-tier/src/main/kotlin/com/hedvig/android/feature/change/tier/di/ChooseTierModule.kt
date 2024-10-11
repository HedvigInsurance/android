package com.hedvig.android.feature.change.tier.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCase
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCaseImpl
import com.hedvig.android.feature.change.tier.data.GetCustomizableInsurancesUseCase
import com.hedvig.android.feature.change.tier.data.GetCustomizableInsurancesUseCaseImpl
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.navigation.SummaryParameters
import com.hedvig.android.feature.change.tier.ui.chooseinsurance.ChooseInsuranceViewModel
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonViewModel
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageViewModel
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierFlowViewModel
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryViewModel
import com.hedvig.android.featureflags.FeatureManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chooseTierModule = module {
  viewModel<SelectCoverageViewModel> { params ->
    SelectCoverageViewModel(
      params = params.get<InsuranceCustomizationParameters>(),
      tierRepository = get<ChangeTierRepository>(),
      getCurrentContractDataUseCase = get<GetCurrentContractDataUseCase>(),
    )
  }

  viewModel<StartTierFlowViewModel> { params ->
    StartTierFlowViewModel(
      insuranceID = params.get<String>(),
      tierRepository = get<ChangeTierRepository>(),
    )
  }

  viewModel<ChooseInsuranceViewModel> {
    ChooseInsuranceViewModel(
      getCustomizableInsurancesUseCase = get<GetCustomizableInsurancesUseCase>(),
      tierRepository = get<ChangeTierRepository>(),
    )
  }

  single<GetCurrentContractDataUseCase> {
    GetCurrentContractDataUseCaseImpl(
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
    )
  }

  single<GetCustomizableInsurancesUseCase> {
    GetCustomizableInsurancesUseCaseImpl(
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
    )
  }

  viewModel<SummaryViewModel> { params ->
    SummaryViewModel(
      params = params.get<SummaryParameters>(),
      tierRepository = get<ChangeTierRepository>(),
      getCurrentContractDataUseCase = get<GetCurrentContractDataUseCase>(),
      downloadPdfUseCase = get<DownloadPdfUseCase>(),
    )
  }

  viewModel<ComparisonViewModel> { params ->
    ComparisonViewModel(
      quoteIds = params.get<List<String>>(),
      tierRepository = get<ChangeTierRepository>(),
    )
  }
}
