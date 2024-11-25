package com.hedvig.android.feature.insurances.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCaseDemo
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCaseImpl
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCaseDemo
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCaseImpl
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceViewModel
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailViewModel
import com.hedvig.android.feature.insurances.insurancedetail.GetContractForContractIdUseCase
import com.hedvig.android.feature.insurances.insurancedetail.GetContractForContractIdUseCaseImpl
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsViewModel
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.notification.badge.data.crosssell.CrossSellCardNotificationBadgeServiceProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val insurancesModule = module {
  viewModel<InsuranceViewModel> {
    InsuranceViewModel(
      get<GetInsuranceContractsUseCaseProvider>(),
      get<GetCrossSellsUseCaseProvider>(),
      get<CrossSellCardNotificationBadgeServiceProvider>(),
      get<ApplicationScope>(),
    )
  }
  viewModel<TerminatedContractsViewModel> {
    TerminatedContractsViewModel(get<GetInsuranceContractsUseCaseProvider>())
  }
  viewModel<ContractDetailViewModel> { (contractId: String) ->
    ContractDetailViewModel(contractId, get<FeatureManager>(), get<GetContractForContractIdUseCase>())
  }
  single<GetContractForContractIdUseCase> {
    GetContractForContractIdUseCaseImpl(get<GetInsuranceContractsUseCaseProvider>())
  }
  provideGetContractsUseCase()
  provideGetCrossSellsUseCase()
}

private fun Module.provideGetContractsUseCase() {
  single<GetInsuranceContractsUseCaseImpl> {
    GetInsuranceContractsUseCaseImpl(
      get<ApolloClient>(),
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
}

private fun Module.provideGetCrossSellsUseCase() {
  single<GetCrossSellsUseCaseImpl> {
    GetCrossSellsUseCaseImpl(get<ApolloClient>())
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
}
