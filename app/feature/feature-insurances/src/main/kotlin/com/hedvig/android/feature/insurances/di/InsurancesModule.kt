package com.hedvig.android.feature.insurances.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.feature.insurances.data.GetCrossSellsDemoUseCaseImpl
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCaseImpl
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsDemoUseCaseImpl
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCaseImpl
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceViewModel
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailViewModel
import com.hedvig.android.feature.insurances.insurancedetail.GetContractDetailsUseCase
import com.hedvig.android.feature.insurances.insurancedetail.coverage.GetContractCoverageUseCase
import com.hedvig.android.feature.insurances.insurancedetail.coverage.GetContractCoverageUseCaseImpl
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsViewModel
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.language.LanguageService
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val insurancesModule = module {
  viewModel<InsuranceViewModel> {
    InsuranceViewModel(
      get<GetInsuranceContractsUseCase>(),
      get<GetCrossSellsUseCase>(),
      get<CrossSellCardNotificationBadgeService>(),
    )
  }
  viewModel<TerminatedContractsViewModel> {
    TerminatedContractsViewModel(get<GetInsuranceContractsUseCase>())
  }
  viewModel<ContractDetailViewModel> { (contractId: String) ->
    ContractDetailViewModel(contractId, get(), get())
  }

  single<GetContractCoverageUseCase> {
    GetContractCoverageUseCaseImpl(get<ApolloClient>(octopusClient))
  }
  single<GetContractDetailsUseCase> {
    GetContractDetailsUseCase(
      get<ApolloClient>(giraffeClient),
      get<GetContractCoverageUseCase>(),
      get<LanguageService>(),
      get<FeatureManager>(),
    )
  }
}

val insuranceContractsUseCaseModule = module {
  single<GetInsuranceContractsUseCase> {
    GetInsuranceContractsUseCaseImpl(
      get<ApolloClient>(giraffeClient),
      get<LanguageService>(),
    )
  }
}

val insuranceContractsDemoUseCaseModule = module {
  single<GetInsuranceContractsUseCase> {
    GetInsuranceContractsDemoUseCaseImpl()
  }
}

val crossSellsUseCaseModule = module {
  single<GetCrossSellsUseCase> {
    GetCrossSellsUseCaseImpl(get<ApolloClient>(octopusClient))
  }
}

val crossSellsDemoUseCaseModule = module {
  single<GetCrossSellsUseCase> {
    GetCrossSellsDemoUseCaseImpl()
  }
}
