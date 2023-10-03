package com.hedvig.android.feature.insurances.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCaseDemo
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCaseImpl
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCaseDemo
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCaseImpl
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceViewModel
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailViewModel
import com.hedvig.android.feature.insurances.insurancedetail.coverage.GetContractCoverageUseCase
import com.hedvig.android.feature.insurances.insurancedetail.coverage.GetContractCoverageUseCaseImpl
import com.hedvig.android.feature.insurances.insurancedetail.data.GetContractDetailsUseCaseDemo
import com.hedvig.android.feature.insurances.insurancedetail.data.GetContractDetailsUseCaseImpl
import com.hedvig.android.feature.insurances.insurancedetail.data.GetContractDetailsUseCaseProvider
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsViewModel
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.language.LanguageService
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val insurancesModule = module {
  viewModel<InsuranceViewModel> {
    InsuranceViewModel(
      get<GetInsuranceContractsUseCaseProvider>(),
      get<GetCrossSellsUseCaseProvider>(),
      get<CrossSellCardNotificationBadgeService>(),
    )
  }
  viewModel<TerminatedContractsViewModel> {
    TerminatedContractsViewModel(get<GetInsuranceContractsUseCase>())
  }
  viewModel<ContractDetailViewModel> { (contractId: String) ->
    ContractDetailViewModel(contractId, get<GetContractDetailsUseCaseProvider>())
  }

  single<GetContractCoverageUseCase> {
    GetContractCoverageUseCaseImpl(get<ApolloClient>(octopusClient))
  }
  single<GetContractDetailsUseCaseImpl> {
    GetContractDetailsUseCaseImpl(
      get<ApolloClient>(giraffeClient),
      get<GetContractCoverageUseCase>(),
      get<LanguageService>(),
      get<FeatureManager>(),
    )
  }
  single<GetInsuranceContractsUseCaseDemo> {
    GetInsuranceContractsUseCaseDemo()
  }
  single {
    GetInsuranceContractsUseCaseProvider(
      demoManager = get<DemoManager>(),
      prodImpl = get<GetInsuranceContractsUseCaseImpl>(),
      demoImpl = get<GetInsuranceContractsUseCaseDemo>(),
    )
  }

  single<GetCrossSellsUseCaseImpl> {
    GetCrossSellsUseCaseImpl(get<ApolloClient>(octopusClient))
  }

  single<GetCrossSellsUseCaseDemo> {
    GetCrossSellsUseCaseDemo()
  }
  single {
    GetCrossSellsUseCaseProvider(
      demoManager = get<DemoManager>(),
      prodImpl = get<GetCrossSellsUseCaseImpl>(),
      demoImpl = get<GetCrossSellsUseCaseDemo>(),
    )
  }
  provideGetContractDetailsUseCase()
}

private fun Module.provideGetContractDetailsUseCase() {
  single<GetContractDetailsUseCaseDemo> {
    GetContractDetailsUseCaseDemo()
  }
  single<GetInsuranceContractsUseCaseImpl> {
    GetInsuranceContractsUseCaseImpl(
      get<ApolloClient>(giraffeClient),
      get<LanguageService>(),
    )
  }
  single<GetContractDetailsUseCaseProvider> {
    GetContractDetailsUseCaseProvider(
      demoManager = get<DemoManager>(),
      demoImpl = get<GetContractDetailsUseCaseDemo>(),
      prodImpl = get<GetContractDetailsUseCaseImpl>(),
    )
  }
}
